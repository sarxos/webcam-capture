<?php

namespace v1\resource;

use tonic\Resource as Resource;
use tonic\Request as Request;
use tonic\Response as Response;


/**
 * Handle tick picture upload for specific user.
 *
 * @author Bartosz Firyn (SarXos)
 */
class Heartbeat extends Resource {

	/**
	 * Receive streaming hartbeat.
	 *
	 * @param RESTRequest request
	 * @return RESTResponse
	 */
	function put(Request $request, $uid) {
		$response = new Response($request);
		$response->code = Response::OK;
		$response->body = $uid;
		return $response;
	}

}
