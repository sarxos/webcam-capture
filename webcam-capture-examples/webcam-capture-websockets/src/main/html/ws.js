$(document).ready(function() {

	var ws = new WebSocket("ws://127.0.0.1:8123/");

	ws.onopen = function(e) {
		if (typeof console !== 'undefined') {
			console.info('WS open');
		}
	};

	ws.onmessage = function (e) {
		
		var data = JSON.parse(e.data),
			type = data.type,
			i = 0,
			$webcams = $('#webcams'),
			$img = null;

		if (typeof console !== 'undefined') {
			console.info('WS message', type);
		}

		if (type === 'list') {
			for (i = 0; i < data.webcams.length; i += 1) {
				$img = $("<img></img>")
					.attr("src", "webcam-capture-logo-small.jpg")
					.attr("alt", data.webcams[i])
					.attr("name", data.webcams[i]);
				$webcams.append($img)
			}
		} else if (type === 'image') {
			$("img[name='" + data.webcam + "']")
				.attr("src", "data:image/jpeg;base64," + data.image)
				.trigger("change");
		}
	};

	ws.onclose = function() {
		if (typeof console !== 'undefined') {
			console.info('WS close');
		}
	};

	ws.onerror = function(err) {
		if (typeof console !== 'undefined') {
			console.info('WS error');
		}
	};
});

