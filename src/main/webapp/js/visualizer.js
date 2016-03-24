var Model = function() {
	var my = this;
	my.board = ko.observable();
	my.influenceColors = ko.observable();
	my.laplaceColors = ko.observable();
	my.botPlayAfterHuman = ko.observable(false);
	
	my.newGame = function() {
		$.getJSON("/newgame", function(data) { 
		    my.update(data);
		})
	}
	my.step = function() {
		$.getJSON("/step", function(data) { 
		    my.update(data);
		})
	}

	my.update = function(data) {
		my.board(data.board);
	    my.influenceColors(data.influenceColors);
	    my.laplaceColors(data.laplaceColors);
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