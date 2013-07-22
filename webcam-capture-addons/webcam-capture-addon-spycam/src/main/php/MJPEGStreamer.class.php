<?php

namespace streaming;

use log\KLogger as KLogger;


/**
 * MJPEG stream reader.
 *
 * @author Bartosz Firyn (SarXos)
 */
class MJPEGStreamer {

	private static $BOUNDARY = 'mjpeg-frame';

	private $log = null;

	private $initialized = false;

	private $running = true;

	private $buffer = array();

	private $name = null;

	private $sock = null;


	public function __construct($name) {
		$this->name = $name;
		$this->sock = $this->buildSockPath($this->name);
		$this->log = new KLogger('files' . DIRECTORY_SEPARATOR . 'log' . DIRECTORY_SEPARATOR . $name . '.log', KLogger::DEBUG);
	}

	public function shutdown() {
		$this->log->info('Removing sock file');
		unlink($this->sock);
	}

	private function buildSockPath($name) {
		return getcwd() . DIRECTORY_SEPARATOR . 'files' . DIRECTORY_SEPARATOR . 'sock' . DIRECTORY_SEPARATOR . $this->name;
	}

	public function init() {

		if (file_exists($this->sock)) {
			$this->log->error("Sock file already exists, concurrent process cannot be started");
			exit();
		}

		$this->log->info('Streamer initialization');

		// connection keep-alive, in other case browser will close it when receive last frame
		header('Connection: keep-alive');

		// disable caches
		header('Cache-Control: no-cache');
		header('Cache-Control: private');
		header('Pragma: no-cache');

		// x-mixed-replace to stream JPEG images
		header('Content-type: multipart/x-mixed-replace; boundary=' . self::$BOUNDARY);

		// set unlimited so PHP doesn't timeout during a long stream
		set_time_limit(0);

		// ignore user abort script
		ignore_user_abort(true);

		@apache_setenv('no-gzip', 1);           // disable apache gzip compression
		@ini_set('zlib.output_compression', 0); // disable PHP zlib compression
		@ini_set('implicit_flush', 1);

		// flush all current buffers
		$k = ob_get_level();
		for ($i = 0; $i < $k; $i++) {
			ob_end_flush();
		}

		register_shutdown_function(array($this, 'shutdown'));

		fclose(fopen($this->sock, 'w'));

		$this->initialized = true;
	}

	private function read() {

		$this->log->debug('Read sock file');

		clearstatcache();
		if (filesize($this->sock) === 0) {
			$this->log->debug('Sock file is empty');
			return;
		}

		$f = fopen($this->sock, 'r');
		flock($f, LOCK_SH);
		$frames = @file($this->sock, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
		flock($f, LOCK_UN);
		fclose($f);

		fclose(fopen($this->sock, 'w'));

		if (!empty($frames)) {
			$this->buffer = array_merge($this->buffer, $frames);
			$this->log->debug('Read ' . count($frames) . ' frames');
		}
	}

 	private function control() {

		$filen = $this->sock . '.ctrl';

		if (!file_exists($filen)) {
			$this->log->debug('Control file does not exist');
			return;
		}

		$f = fopen($filen, 'r');
		flock($f, LOCK_SH);
		$config = @json_decode(file_get_contents($filen), true);
		flock($f, LOCK_UN);
		fclose($f);

		$this->log->debug('Read control file ' . print_r($config, true));

		if (!empty($config)) {
			if (isset($config['status']) && $config['status'] !== 'running') {
				$this->log->debug('Found close directive, set running false');
				$this->running = false;
			}
		} else {
			$this->log->fatal('Control configuration is empty!');
			$this->log->fatal('Control configuration is empty!');
		}
	}

	public function serve() {

		$this->log->info('MJPEG streaming started');

		if (!$this->initialized) {
			$this->init();
		}

		while (true) {

			if(connection_status() != CONNECTION_NORMAL) {
				$this->log->debug('Connection closed');
				//break;
			}

			$this->control();
			$this->read();

			$this->log->debug('Frames buffer size is ' . count($this->buffer));

			if (!empty($this->buffer)) {
				$frame = array_shift($this->buffer);
				$content = file_get_contents($frame);
				print('--' . self::$BOUNDARY . "\nContent-type: image/jpeg\n\n" . $content);
				flush();
			}

			sleep(1);

			if (!$this->running) {
				$this->log->info('Stopping');
				break;
			}
		}

		exit(0);
	}
}




