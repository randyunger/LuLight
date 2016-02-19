/**
 * Created by Unger on 10/18/15.
 */

$(document).ready(function(){
    $("input.range").change(function() {
        $(this).parent().siblings(".level").text(this.value);

        var url= "set/" + this.name + "/" + this.value;

        $.ajax({
            url: url,
            type: 'POST'
        });
    });
});

$(document).ready(function(){
    $("input.filtered").change(function() {
        $(this).parent().siblings(".level").text(this.value);
        var filters = JSON.parse($("h2").attr("data"));
        //Add filter for the current event
        filters.bulbType = {};
        filters.bulbType.code = this.name;
        var jsonString = JSON.stringify(filters);

        var url = "filtered?level="+this.value;

        $.ajax({
            url: url,
            type: 'POST',
            data: jsonString
        });
    });
});


$(document).ready(function(){
    $("#toggleId").change(function() {
        $(".id").css("visibility", "visible")
    })
});

$(document).ready(function(){
    var url = "state";
    $.ajax({
        url: url
    }).done(function(state) {
        window.state = state;
        setRangesFromState(state);
    });
});

function setRangesFromState(state) {
    $(".range").each(function(ix, slider){
        var loadId = slider.name;
        var level = state[loadId].level;
        slider.value = level;
        $(slider).parent().siblings(".level").text(level);
    })
}

$(document).ready(function(){
    $("div.scene").click(function() {
        //$(this).parent().siblings(".level").text(this.value);

        var url= "scene/" + this.attributes["name"].textContent; //+ "/" + this.value;

        $.ajax({
            url: url,
            type: 'POST'
        });
    });
});

