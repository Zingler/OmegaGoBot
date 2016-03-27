<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/knockout/3.4.0/knockout-min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
<script type="text/javascript" src="/js/visualizer.js"></script>

<style>
.tables {
	flex-wrap: wrap;
	justify-context: space-around;
	display: flex;
}
.tables .section {
	padding: 10px;
}

table {
    width: 400px;
    height: 400px;
    
}
table, td {
 border: 1px solid black;
 border-collapse: collapse;
}

table td {
	width: 4.3%;
	height: 15px;
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
Number of turns: <input data-bind="value: numSteps"/>
<p>Have bot play after human move?
<input type="checkbox" data-bind="checked: botPlayAfterHuman"/>
</p>

<p>Last move played was of type: <span data-bind="text: lastMoveType"></span></p>
<p>Time taken: <span data-bind="text: time"></span>ms</p>

<div class="tables">
<div class="section">
Board
<table>
<tbody data-bind="foreach: board">
	<tr data-bind="foreach: $data">
		<td data-bind="css: { lastMove: lastMove, consideredMove: consideredMove }, click: $root.play">
			<span data-bind="text: symbol"></span>
		</td>
	</tr>
</tbody>
</table>
</div>

<!-- ko foreach: boardViews -->
<div class="section">
<span data-bind="text: label"></span>
<table>
<tbody data-bind="foreach: cells">
	<tr data-bind="foreach: $data">
		<td data-bind="style: {backgroundColor: color}, attr: {title: value}">
			<span data-bind="text: symbol"></span>
		</td>
	</tr>
</tbody>
</table>
</div>
<!-- /ko -->

</div>

</body>
<script>
ko.applyBindings(myModel)
</script>
</html>