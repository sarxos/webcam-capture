<?php

# Used to separate multipart
$boundary = "spycam-mjpeg-stream";

# We start with the standard headers. PHP allows us this much
header("Cache-Control: no-cache");
header("Cache-Control: private");
header("Pragma: no-cache");
header("Content-type: multipart/x-mixed-replace; boundary=$boundary");

# From here out, we no longer expect to be able to use the header() function
print "--$boundary\n";

# set unlimited so PHP doesn't timeout during a long stream
set_time_limit(0);

@apache_setenv('no-gzip', 1);                // disable apache gzip compression
@ini_set('zlib.output_compression', 0);      // disable PHP zlib compression
@ini_set('implicit_flush', 1);               // set implicit flush 

// and flush all current buffers

for ($i = 0; $i < ob_get_level(); $i++) {
    ob_end_flush();
}

ob_implicit_flush(1);


// streaming

$files = scandir('uploads');
$i = 0;

# The loop, producing one jpeg frame per iteration
while (true) {

	if ($i >= count($files)) {
		break;
	}
    
	$file = $files[$i++];
	if (substr($file, strlen($file) - 4, 4) !== '.jpg') {
		continue;
	}
	
	# Per-image header, note the two new-lines
    print("Content-type: image/jpeg\n\n");
	//print("Content-type: text/plain\n\n");

    # Your function to get one jpeg image
    print(file_get_contents("uploads/$file"));
	//print("uploads/$file");

	//flush();
	//ob_flush();
	usleep(200000);

    # The separator
    print("--$boundary\n");
	
}
