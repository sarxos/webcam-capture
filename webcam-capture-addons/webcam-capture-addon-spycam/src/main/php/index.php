<?php

include_once('config.php');


if (!isset($_FILES['picture'])) {
	die('No picture');
}

if (!isset($_POST['passwd'])) {
	die('Wrong password');
} else {
	if ($_POST['passwd'] !== $SPY_CONFIG['passwd']) {
		die('Incorrect password');
	}
}

$file = $_FILES['picture'];
$dst_name = $file['name'];
$tmp_name = $file['tmp_name'];


if (!preg_match('/^[0-9]+\.jpg$/', $dst_name)) {
	die('Missing picture');
}


$dir = $SPY_CONFIG['dir'];
if (substr($dir, strlen($dir) - 1, 1) !== '/') {
	$dir = $dir . '/';
}




move_uploaded_file($tmp_name, $dir . $dst_name);

print('ok');
