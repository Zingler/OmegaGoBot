var Model = function() {
	var my = this;
	my.board = ko.observable();
	my.boardViews = ko.observable([]);
	my.botPlayAfterHuman = ko.observable(false);
	my.numSteps = ko.observable(1);
	my.lastMoveType = ko.observable();
	my.time = ko.observable();
	
	my.newGame = function() {
		$.getJSON("/newgame", function(data) { 
		    my.update(data);
		})
	}
	my.step = function() {
		$.getJSON("/step?steps="+my.numSteps(), function(data) { 
		    my.update(data);
		})
	}

	my.update = function(data) {
		my.board(data.board);
	    my.boardViews(data.boardViews);
	    my.lastMoveType(data.lastMoveType);
	    my.time(data.time);
	}
	
	my.play = function(data, event) {
		var context = ko.contextFor(event.target);
		var row = context.$parentContext.$index();
		var col = context.$index();
		$.getJSON("/play?row="+row+"&col="+col+"&botPlayAfterHuman="+my.botPlayAfterHuman(), function(data) { 
		    my.update(data);
		})
	}
}

myModel = new Model();
myModel.newGame();