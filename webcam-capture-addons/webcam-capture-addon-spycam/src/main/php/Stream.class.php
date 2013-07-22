<?php

namespace v1\resource;

use streaming\MJPEGStreamer;

use tonic\Resource as Resource;
use tonic\Request as Request;
use tonic\Response as Response;
use \PDO as PDO;

/**
 * @author Bartosz Firyn (SarXos)
 */
class Stream extends Resource {

	/**
	 * Serve MJPEG stream.
	 *
	 * @param RESTRequest request
	 * @param Request $request
	 * @param string $uid - user ID
	 * @param string $camid - camera ID
	 * @return RESTResponse
	 */
	function get(Request $request, $uid, $camid) {

		if ($this->canAcess($uid, $camid)) {
			$streamer = new MJPEGStreamer($uid . '.' . $camid);
			$streamer->serve();
		} else {
			$response = new Response($request);
			$response->code = Response::FORBIDDEN;
			$response->body = 'You are not authorized to access this resource';
			return $response;
		}
	}

	private function canAcess($uid, $camid) {

		$uid = mysql_real_escape_string($uid);
		$camid = mysql_real_escape_string($camid);

		$pdo = new PDO('mysql:host=localhost;dbname=spycam', 'root', 'secret');

		$sth = $pdo->prepare('select * from users where uid = :uid');
		$sth->execute(array(':uid' => $uid));

		$user = $sth->fetch();

		$sth = $pdo->prepare('select * from cams where uid = :uid and camid = :camid');
		$sth->execute(array(':uid' => $uid, ':camid' => $camid));

		$camera = $sth->fetch();

		// TODO validate passwd

		if ($user !== false && $camera !== false) {
			return true;
		} else {
			return false;
		}
	}

}
