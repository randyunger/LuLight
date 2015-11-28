/**
 * Created by Unger on 10/18/15.
 */

$(document).ready(function(){
    $("input").change(function() {
        $(this).parent().siblings(".level").text(this.value);

        //var onOff = "on";
        //if(this.value >= 50)
        //    onOff = "off";

        var url= "set/" + this.name + "/" + this.value;

        $.ajax({
            url: url,
            type: 'POST'
        });
        //var enable = $(this).is(":checked");
        //var output = $(this).attr("outputId");
        //
        //if(enable) {
        //    var input = $(this).attr("inputId");
        //    var url = "tie/"+output+"/"+input;
        //    $.ajax({
        //        url: url,
        //        type: 'POST'
        //        //data: { strID:$(this).attr("id"), strState:state }
        //    });
        //} else {
        //    var url = "tie/"+output;
        //    $.ajax({
        //        url: url,
        //        type: 'DELETE'
        //    });
        //}


    });
});

$(document).ready(function(){
    $("#allOff").click(function() {
        var url = "tie";
        $.ajax({
            url: url,
            type: 'DELETE'
        });
        location.reload();
    })
});

$(document).ready(function(){
    $("#refresh").click(function() {
        location.reload();
    })
});