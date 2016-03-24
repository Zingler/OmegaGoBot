<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/knockout/3.4.0/knockout-min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
<script type="text/javascript" src="/js/visualizer.js"></script>

<style>
table {
    width: 400px;
    height: 400px;
    table-layout: fixed;
     float: left;
}
table, td {
 border: 1px solid black;
 border-collapse: collapse;
}

table td {
	width: 4.3%;
	height: 20px;
	text-align: center;
    padding: 0;
}

.lastMove {
	background-color: yellow;
}
.consideredMove {
	background-color: grey;
}
</style>

</head>

<body>

<button data-bind="click: step" >Advance turn</button>
<p>Have bot play after human move?
<input type="checkbox" data-bind="checked: botPlayAfterHuman"/>
</p>

<div>
<table>
<tbody data-bind="foreach: board">
	<tr data-bind="foreach: $data">
		<td data-bind="css: { lastMove: lastMove, consideredMove: consideredMove }, click: $root.play">
			<span data-bind="text: symbol"></span>
		</td>
	</tr>
</tbody>
</table>

<table>
<tbody data-bind="foreach: influenceColors">
	<tr data-bind="foreach: $data">
		<td data-bind="style: {backgroundColor: $data}"></td>
	</tr>
</tbody>
</table>
<div>

<div>
<table>
<tbody data-bind="foreach: laplaceColors">
	<tr data-bind="foreach: $data">
		<td data-bind="style: {backgroundColor: $data}"></td>
	</tr>
</tbody>
</table>
<div>

</body>
<script>
ko.applyBindings(myModel)
</script>
</html>