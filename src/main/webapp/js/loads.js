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
        filters.bulbTypes = [];
        filters.bulbTypes[0] = {};
        filters.bulbTypes[0].code = this.name;
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

//$(document).ready(function(){
//    var url = "state";
//    $.ajax({
//        url: url
//    }).done(function(state) {
//        window.state = state;
//        setRangesFromState(state);
//    });
//});

function setRangesFromState(state) {
    $(".range").each(function(ix, slider){
        var loadId = slider.name;
        var level = state[loadId].level;
        slider.value = level;
        $(slider).parent().siblings(".level").text(level);
    })
}

function setRangeFromLoadState(loadState) {
    var id = loadState.id;
    var level = loadState.level;
    var rangeId = "#range" + id;

    $(rangeId).each(function(ix, slider){
        slider.value = level;
        $(slider).parent().siblings(".level").text(level);
    })
};


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


$(document).ready(function(){
    // Create a client instance
    //var client = new Paho.MQTT.Client(location.hostname, Number(location.port), "clientId");
    console.log("Booting Paho")
    var client = new Paho.MQTT.Client("localhost", 8888, "jsClient-123");

// set callback handlers
    client.onConnectionLost = onConnectionLost;
    client.onMessageArrived = onMessageArrived;

// connect the client
    client.connect({onSuccess:onConnect});


// called when the client connects
    function onConnect() {
        // Once a connection has been made, make a subscription and send a message.
        console.log("onConnect");
        client.subscribe("test/#", {qos:2});
        client.subscribe("tX", {qos:2});
        client.subscribe("/ha/lights/10228/#", {qos:2});
        message = new Paho.MQTT.Message("Hello sub a");
        message.qos = 2;
        message.destinationName = "test/a";
        client.send(message);
        message2 = new Paho.MQTT.Message("Hello sub b");
        message2.qos = 2;
        message2.destinationName = "test/sub/b";
        client.send(message2);

        messageX = new Paho.MQTT.Message("Hello XX second subscribe");
        messageX.qos = 2;
        messageX.destinationName = "tX";
        client.send(messageX);

        messageX = new Paho.MQTT.Message("Hello no sub");
        messageX.qos = 2;
        messageX.destinationName = "tNoSub";
        client.send(messageX);

        messageLights = new Paho.MQTT.Message("json message");
        messageLights.destinationName = "/ha/lights/10228/7";
        client.send(messageLights);
    }

// called when the client loses its connection
    function onConnectionLost(responseObject) {
        if (responseObject.errorCode !== 0) {
            console.log("onConnectionLost:"+responseObject.errorMessage);
        }
        window.location.reload();
    }

// called when a message arrives
    function onMessageArrived(message) {
        console.log("onMessageArrived:"+message.payloadString);
        //console.log(message)
        if(message.payloadString.indexOf("{") > -1) {   //detect jason
            var msgObj = JSON.parse(message.payloadString);
            setRangeFromLoadState(msgObj)
        }
    }
});
