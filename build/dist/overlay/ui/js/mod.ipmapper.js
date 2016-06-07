/*!
 * IP Address geocoding API for Google Maps
 * http://lab.abhinayrathore.com/ipmapper/
 * Last Updated: June 13, 2012
 */
var apolygon = null;
var allMarkers = [];
var polygons = [];
var allBanned = [];


var IPMapper = {
    map: null,
    mapTypeId: google.maps.MapTypeId.ROADMAP,
    latlngbound: null,
    infowindow: null,
    baseUrl: "http://freegeoip.net/json/",
    initializeMap: function(mapId){
        IPMapper.latlngbound = new google.maps.LatLngBounds();
        var latlng = new google.maps.LatLng(0, 0);
        //set Map options
        var mapOptions = {
            zoom: 2,
            center: latlng,
            styles: [{"featureType":"administrative","elementType":"labels.text.fill","stylers":[{"color":"#444444"}]},{"featureType":"landscape","elementType":"all","stylers":[{"color":"#f2f2f2"}]},{"featureType":"poi","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"road","elementType":"all","stylers":[{"saturation":-100},{"lightness":45}]},{"featureType":"road.highway","elementType":"all","stylers":[{"visibility":"simplified"}]},{"featureType":"road.arterial","elementType":"labels.icon","stylers":[{"visibility":"off"}]},{"featureType":"transit","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"water","elementType":"all","stylers":[{"color":"#2BA4C7"},{"visibility":"on"}]}],
            mapTypeId: IPMapper.mapTypeId
        }
        //init Map
        IPMapper.map = new google.maps.Map(document.getElementById(mapId), mapOptions);
        //init info window
        IPMapper.infowindow = new google.maps.InfoWindow();
        
        //this block controls drawing functions
        var drawingManager = new google.maps.drawing.DrawingManager({
            drawingMode: null,
            drawingControl: true,
            drawingControlOptions: {
                position: google.maps.ControlPosition.TOP_CENTER,
                drawingModes: [
                    google.maps.drawing.OverlayType.MARKER,
                    google.maps.drawing.OverlayType.CIRCLE,
                    google.maps.drawing.OverlayType.POLYGON,
                    google.maps.drawing.OverlayType.POLYLINE,
                    google.maps.drawing.OverlayType.RECTANGLE
                ]
            },
            markerOptions: {icon: 'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png'},
            circleOptions: {
                fillColor: '#99ff66',
                fillOpacity: .5,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                zIndex: 1,
                strokeColor:'green'
            },
            polygonOptions:{
                fillColor: '#F00',
                fillOpacity: .4,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: true,
                strokeColor:'#800000'
            },
            rectangleOptions:{
                fillColor: '#F00',
                fillOpacity: .4,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: true,
                strokeColor:'#800000'
            }

        });
        drawingManager.setMap(this.map);
        //info window close event
        google.maps.event.addListener(IPMapper.infowindow, 'closeclick', function() {
            IPMapper.map.fitBounds(IPMapper.latlngbound);
            IPMapper.map.panToBounds(IPMapper.latlngbound);
        });

        //listen for user to finish drawing polygon
        google.maps.event.addListener(drawingManager,'polygoncomplete',function(polygon) {
            //mvc array
            // var coordinates = polygon.getPath();

            // console.log("path is ... ", JSON.stringify(polygon.getPath().getArray()));
            // var encodeString = google.maps.geometry.encoding.encodePath(path);
            // if (encodeString) {
            //     document.getElementById('encoded-polyline').value = encodeString;
            // }

            apolygon = polygon;

            var coords = google.maps.geometry.encoding.encodePath(polygon.getPath());
            var toStore = JSON.stringify({"type": "POLYGON", "path": coords});
            $.ajax({
                type: "POST",
                contentType:"application/x-www-form-urlencoded",
                url: "plugin/geoMap/processShape",
                beforeSend: function (request) {
                    request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
                },
                data: {
                    json: toStore
                }
            }).done(function(out){
                console.log("UPDATE WORKED!");

            });
            //how we confirm if point is banned or not
            // google.maps.geometry.poly.containsLocation(allMarkers[14].getPosition(), apolygon);
        });
    },
    addIPArray: function(ipArray){
        ipArray = IPMapper.uniqueArray(ipArray); //get unique array elements
        //add Map Marker for each IP
        for (var i = 0; i < ipArray.length; i++){
            IPMapper.addIPMarker(ipArray[i]);
        }
    },
    addIPMarkerCustom: function (ip) {
        // note* this will make another external call
        $.ajax({
            method: "GET",
            crossDomain: true,
            dataType : 'jsonp',
            url: "http://freegeoip.net/json/"+ip
            // url: "plugin/geoMap/processLoginUsingHeaders"
        })
            .done(function (jsonObj) {
                var json = JSON.stringify(jsonObj);
                IPMapper.placeIPMarkerFromJSON(json);
                // console.log("ADDING CUSTOM!!");
            })

    },
    placeShapesFromJSON: function(data){

        console.log(data + " is shape data recieved");

        var shapes = $.parseJSON(data);
        for(var x = 0; x<shapes.length; x++){
            place(shapes[x]);

        }
        function place(item) {
            var shape = new google.maps.Polygon({ //create Map Marker
                map: IPMapper.map,
                fillColor: '#F00',
                fillOpacity: .4,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: true,
                strokeColor: '#800000',
                path: google.maps.geometry.encoding.decodePath(item["PATH"])
            });
            polygons.push(shape);
        }
    },
    placeIPMarker: function(marker, latlng, contentString){ //place Marker on Map
        marker.setPosition(latlng);
        google.maps.event.addListener(marker, 'click', function() {
            IPMapper.getIPInfoWindowEvent(marker, contentString);
        });
        IPMapper.latlngbound.extend(latlng);
        IPMapper.map.setCenter(IPMapper.latlngbound.getCenter());
        IPMapper.map.fitBounds(IPMapper.latlngbound);
    },
    getIPInfoWindowEvent: function(marker, contentString){ //open Marker Info Window
        IPMapper.infowindow.close()
        IPMapper.infowindow.setContent(contentString);
        IPMapper.infowindow.open(IPMapper.map, marker);
    },
    uniqueArray: function(inputArray){ //return unique elements from Array
        var a = [];
        for(var i=0; i<inputArray.length; i++) {
            for(var j=i+1; j<inputArray.length; j++) {
                if (inputArray[i] === inputArray[j]) j = ++i;
            }
            a.push(inputArray[i]);
        }
        return a;
    },
    logError: function(error){
        if (typeof console == 'object') { console.error(error); }
    },
    getIPJSON: function(ip) {
        ipRegex = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
        if($.trim(ip) != '' && ipRegex.test(ip)) { //validate IP Address format
            var url = encodeURI(IPMapper.baseUrl + ip + "?callback=?"); //geocoding url
            $.getJSON(url, function (data) { //get Geocoded JSONP data
                if ($.trim(data.latitude) != '' && data.latitude != '0' && !isNaN(data.latitude)) { //Geocoding successfull
                    // if successful, use this json
                    return $.stringifyJSON(data);
                } else {
                    IPMapper.logError('IP Address geocoding failed!');
                    $.error('IP Address geocoding failed!');
                }
            });
        }
    },
    upadateBans: function(json){
        //update db with who is banned now



    },
    placeIPMarkerFromJSON: function(json) {
        var ct = 0;
        function place(data) {
            var banned = false;
            var latitude = data.latitude;
            var longitude = data.longitude;
            // var latitude = data.lat;
            // var longitude = data.lng;
            var contentString = "";
            $.each(data, function mark(key, val) {
                // if(key == "Identity Url"){
                if(key == "identity"){
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> <a href="/identityiq/define/identity/identity.jsf?id='+val+'">'+val+'</a><br />';
                }
                else {
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> ' + val + '<br />';
                }
            })
            var latlng = new google.maps.LatLng(latitude, longitude);
            if(polygons.length > 0){
                for(var x  = 0; x< polygons.length; x++) {
                    if (google.maps.geometry.poly.containsLocation(latlng, polygons[x])) {
                        banned = true;
                    }
                }

            }
            var marker = new google.maps.Marker({ //create Map Marker
                map: IPMapper.map,
                draggable: false,
                position: latlng,
                title: (data["user_name"]||data["ip"]),
                id: (data['id'] || data["ip"]),
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                        scale: 6.5,
                        fillColor: banned? "#000000" : "#F00",
                        fillOpacity:  banned? 0.6 : 0.4,
                        strokeWeight: 0.5
                        // strokeColor: 'green'
                }
            })
            // IPMapper.placeIPMarker(marker, latlng, contentString).done(allMarkers.push(marker)); //place Marker on Map
            IPMapper.placeIPMarker(marker, latlng, contentString); //place Marker on Map
            ct +=1;
            if(marker){
                allMarkers.push(marker);
                if(banned){
                    allBanned.push(marker);
                }
            }

        }
        var pairs= $.parseJSON(json);
        if (pairs && pairs.constructor === Array) {
            for (var x = 0; x < pairs.length; x++) {
                place(pairs[x]);
            }
        }
        else if(pairs){
            place(pairs);
        }
        else{
            throw "Error: bad ip data!";
        }
    },
    swap_color: function(name){
        var icon1 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 6.5,
            fillColor: "#F00",
            fillOpacity: 0.4,
            strokeWeight: 0.5
            // strokeColor: 'green'
        }
        var icon2 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 7.0,
            fillColor: "#ff6600",
            fillOpacity: 0.7,
            strokeWeight: 0.6
        }

        if(name == ''){
            for(var x =0; x<allMarkers.length; x++){
                allMarkers[x].setIcon(icon1);
            }

        }
        else {
            for (var x = 0; x < allMarkers.length; x++) {
                if (allMarkers[x].title.search(new RegExp(name, "i")) > -1) {
                    console.log(allMarkers[x].name);
                    allMarkers[x].setIcon(icon2);
                }
                else {
                    allMarkers[x].setIcon(icon1);
                }
            }
        }
    }
}