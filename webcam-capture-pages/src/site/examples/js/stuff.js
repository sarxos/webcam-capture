
function renderJSON(model) {
	$('#json').html(JSON.prettyFormat('model', model.attributes));
}

function encodeEntities(s){
	return $("<div/>").text(s).html().replace(/^(\r\n|\n|\r)*/igm,"");
}

$('#html-code').html(encodeEntities($('#html-snippet').html()));
$('#js-code').html(encodeEntities($('#js-snippet').text()));

$('head').append('<script type="text/javascript">' + $('#js-snippet').text() + '</script>');

prettyPrint();
