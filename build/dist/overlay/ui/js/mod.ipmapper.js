/*!
 * IP Address geocoding API for Google Maps
 * http://lab.abhinayrathore.com/ipmapper/
 * Last Updated: June 13, 2012
 */
var allMarkers = {};
var polygons = {};
var modified = {};
var glob;
var IPMapper = {
    map: null,
    mapTypeId: google.maps.MapTypeId.ROADMAP,
    latlngbound: null,
    infowindow: null,
    baseUrl: "http://freegeoip.net/json/",
    initializeMap: function(mapId){
        IPMapper.latlngbound = new google.maps.LatLngBounds();
        var latlng = new google.maps.LatLng(18.83, -38.131);
        //set Map options
        var mapOptions = {
            zoom: 2,
            center: latlng,
            styles: [{"featureType":"administrative","elementType":"labels.text.fill","stylers":[{"color":"#444444"}]},{"featureType":"landscape","elementType":"all","stylers":[{"color":"#f2f2f2"}]},{"featureType":"poi","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"road","elementType":"all","stylers":[{"saturation":-100},{"lightness":45}]},{"featureType":"road.highway","elementType":"all","stylers":[{"visibility":"simplified"}]},{"featureType":"road.arterial","elementType":"labels.icon","stylers":[{"visibility":"off"}]},{"featureType":"transit","elementType":"all","stylers":[{"visibility":"off"}]},{"featureType":"water","elementType":"all","stylers":[{"color":"#2BA4C7"},{"visibility":"on"}]}],
            mapTypeId: IPMapper.mapTypeId,
            minZoom: 3

    };
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
                    // google.maps.drawing.OverlayType.MARKER,
                    // google.maps.drawing.OverlayType.CIRCLE,
                    google.maps.drawing.OverlayType.POLYGON,
                    // google.maps.drawing.OverlayType.POLYLINE,
                    // google.maps.drawing.OverlayType.RECTANGLE
                ]
            },
            polygonOptions:{
                fillColor: '#F00',
                fillOpacity: .4,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: false,
                strokeColor:'#800000'
            },
            rectangleOptions:{
                fillColor: '#F00',
                fillOpacity: .4,
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: false,
                strokeColor:'#800000'
            }
        });
        drawingManager.setMap(this.map);
        //info window close event
        google.maps.event.addListener(IPMapper.infowindow, 'closeclick', function() {
            IPMapper.map.fitBounds(IPMapper.latlngbound);
            IPMapper.map.panToBounds(IPMapper.latlngbound);
        });
        google.maps.event.addListener(IPMapper.map, 'bounds_changed', function() {
            google.maps.event.trigger(IPMapper.map, 'resize');
        });

        //listen for user to finish drawing polygon
        google.maps.event.addListener(drawingManager,'polygoncomplete',function(polygon) {
            google.maps.event.addListener(polygon, 'click', function(){IPMapper.destroyShape(polygon)});
            polygon.id = google.maps.geometry.encoding.encodePath(polygon.getPath());

            polygon.getPaths().forEach(function(path, index){
                google.maps.event.addListener(path, 'insert_at', function(){
                    IPMapper.updateShapePath(polygon.id, polygon.getPath());
                });
                google.maps.event.addListener(path, 'remove_at', function(){
                    IPMapper.updateShapePath(polygon, polygon.getPath());
                });
                google.maps.event.addListener(path, 'set_at', function(){
                    IPMapper.updateShapePath(polygon, polygon.getPath());
                });
            });


            for(var key in allMarkers ){
                if(google.maps.geometry.poly.containsLocation(allMarkers[key].getPosition(), polygon)){
                    allMarkers[key].banned += 1;
                    allMarkers[key].polys.push(polygon.id);
                    IPMapper.addBan(allMarkers[key]);
                    modified[key] = allMarkers[key];
                }
            }
            IPMapper.colorCode(modified);
            polygons[polygon.id] = polygon;
            var coords = polygon.getPath().getArray();
            // var toStore = JSON.stringify({"ID": google.maps.geometry.encoding.encodePath(polygon.getPath()), "type": "POLYGON", "path": coords});
            var toStore = JSON.stringify({"ID": google.maps.geometry.encoding.encodePath(polygon.getPath()), "type": "POLYGON", "path": coords});
            $.ajax({
                type: "POST",
                contentType:"application/x-www-form-urlencoded",
                url: "plugin/geoMap/processShape",
                beforeSend: function (request) {
                    request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
                },
                data: {
                    json: toStore
                },
                success: function(data){
                    console.log(data + " IS DATA FROM ADDING POLYGON"); //will alert ok
                }
            }).done(function(out){
                console.log("UPDATE WORKED!");

            });
            //how we confirm if point is banned or not
            // google.maps.geometry.poly.containsLocation(allMarkers[14].getPosition(), apolygon);
        });
    },
    destroyShape: function(polygon){
        delete polygons[polygon.id];

        var toStore = JSON.stringify({"ID": polygon.id});
        // var decision = confirm("Destroy Ban Region?");
        bootbox.confirm("Destroy Ban Region?", function(result) {
            if (result) {
                $.ajax({
                    type: "POST",
                    contentType:"application/x-www-form-urlencoded",
                    url: "plugin/geoMap/killShape",
                    beforeSend: function (request) {
                        request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
                    },
                    data: {
                        json: toStore
                    },
                    success: function(data){
                        console.log(data + " IS DATA FROM deleting POLYGON"); //will alert ok
                    }
                }).done(function(out){
                    console.log("UPDATE WORKED!");

                });
                for(var key in allMarkers){
                    if(google.maps.geometry.poly.containsLocation(allMarkers[key].getPosition(), polygon)) {
                        IPMapper.oneRem(key);
                        modified[key] = allMarkers[key];
                    }
                }
            polygon.setMap(null);
            IPMapper.colorCode(modified);
            }
        });
    },
    colorOne: function(marker_id){
        var icon2 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 6.5,
            fillColor: "#F00",
            fillOpacity: 0.4,
            strokeWeight: 0.5
            // strokeColor: 'green'
        };
        var icon1 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 6.5,
            fillColor: "#00",
            fillOpacity: 0.6,
            strokeWeight: 0.5
        };
        var marker = allMarkers[marker_id];
        if(marker.banned >  0){
            marker.setIcon(icon1);
        }
        else{
            marker.setIcon(icon2);
        }
    },
    colorCode: function(list){
        var icon2 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 6.5,
            fillColor: "#F00",
            fillOpacity: 0.4,
            strokeWeight: 0.5
            // strokeColor: 'green'
        };
        var icon1 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 6.5,
            fillColor: "#00",
            fillOpacity: 0.6,
            strokeWeight: 0.5
        };
        // if(list.length > 0){
            // for(var x = 0; x<list.length;x++){
        for(var key in list){
                if(modified[key].banned > 0)
                    modified[key].setIcon(icon1);
                else
                    modified[key].setIcon(icon2);
            }
    },
    addBan: function(marker){
        //TODO: put polygon id in here as well!!!
        var toStore = JSON.stringify({"id" : marker.id});
        $.ajax({
            type: "POST",
            contentType:"application/x-www-form-urlencoded",
            url: "plugin/geoMap/addBan",
            beforeSend: function (request) {
                request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
            },
            data: {
                json: toStore
            }
        }).done(function(out){
            console.log("UPDATE BANS WORKED!");

        });
    },
    updateShapePath: function(id, path){
        console.log("UPDATING MAN", id , " is the id");
        var toStore = JSON.stringify({"id" : id, "path" : path.getArray()});
        // var coords = polygon.getPath().getArray();
        $.ajax({
            type: "POST",
            contentType:"application/x-www-form-urlencoded",
            url: "plugin/geoMap/updateShape",
            beforeSend: function (request) {
                request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
            },
            data: {
                json: toStore
            }
        }).done(function(out){
            console.log("UPDATE SHAPE WORKED!");
        });
    },
    removeBan: function(marker){
        var toStore = JSON.stringify({"id" : marker.id});
        $.ajax({
            type: "POST",
            contentType:"application/x-www-form-urlencoded",
            url: "plugin/geoMap/removeBan",
            beforeSend: function (request) {
                request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
            },
            data: {
                json: toStore
            }
        }).done(function(out){
            console.log("UPDATE BANS WORKED!");

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
        var shapes = $.parseJSON(data);

        for(var x in shapes){
            place(shapes[x])
        }
        function place(item) {
            var shape = new google.maps.Polygon({ //create Map Marker
                map: IPMapper.map,
                fillColor: '#F00',
                fillOpacity: .4,
                id: item["ID"],
                strokeWeight: 2,
                clickable: true,
                editable: true,
                draggable: false,
                strokeColor: '#800000',
                path: eval(item["PATH"])
            });
            polygons[item["ID"]] = shape;
            google.maps.event.addListener(shape, 'click', function(){ IPMapper.destroyShape(shape)});
            polygons[item["ID"]].getPaths().forEach(function(path, index){
            google.maps.event.addListener(path, 'insert_at', function(){
                    IPMapper.updateShapePath(item["ID"], polygons[item["ID"]].getPath());
                });
                google.maps.event.addListener(path, 'remove_at', function(){
                    IPMapper.updateShapePath(item["ID"], polygons[item["ID"]].getPath());
                });
                google.maps.event.addListener(path, 'set_at', function(){
                    IPMapper.updateShapePath(item["ID"], polygons[item["ID"]].getPath());
                });
            });

            // google.maps.event.addListener(shape, 'set_at', function(){
            //     IPMapper.destroyShape(shape)});
            // google.maps.event.addListener(shape, 'insert_at', function(){console.log("EDITEDD!!!")});
            // google.maps.event.addListener(shape, 'set_at', function(){console.log("EDITEDD!!!")});
        }
    },
    oneRem: function(id, zero){
        if(zero == 3883) {
            for (var x = 0; x < allMarkers[id].banned; x++) {
                IPMapper.removeBan(allMarkers[id]);
            }
            allMarkers[id].banned = 0;
            IPMapper.colorOne(id)
        }
        else {
            IPMapper.removeBan(allMarkers[id]);
            allMarkers[id].banned = Math.max(0, allMarkers[id].banned -1);
            IPMapper.colorOne(id);
        }
    },
    oneBan: function(id){
        IPMapper.addBan(allMarkers[id]);
        allMarkers[id].banned +=1;
        IPMapper.colorOne(id);
    },
    toggle_ban: function(chk_bx, markerid){
        if(chk_bx.checked) {
            IPMapper.oneBan(markerid);
        }else{
            IPMapper.oneRem(markerid, 3883);
        }
    },
    placeIPMarker: function(marker, latlng, contentString){
        marker.setPosition(latlng);
        if(marker.banned > 0)
            contentString += '<div id="fancy"> <span class="bigcheck"> <label class="bigcheck"><input type="checkbox" checked class="bigcheck" onchange="IPMapper.toggle_ban(this,\''+marker.id+'\');" name="cheese" value="yes"/> <span class="bigcheck-target"></span> </label> </span></div>';
        else
            contentString += '<div id="fancy"><span class="bigcheck"> <label class="bigcheck"><input type="checkbox" class="bigcheck" onchange="IPMapper.toggle_ban(this,\''+marker.id+'\');" name="cheese" value="yes"/> <span class="bigcheck-target"></span> </label> </span></div>';

        google.maps.event.addListener(marker, 'click', function() {
            IPMapper.getIPInfoWindowEvent(marker, contentString);
            IPMapper.map.panTo(marker.getPosition());
        });
        IPMapper.latlngbound.extend(latlng);
        IPMapper.map.setCenter(IPMapper.latlngbound.getCenter());
        IPMapper.map.fitBounds(IPMapper.latlngbound);
    },
    getIPInfoWindowEvent: function(marker, contentString){ //open Marker Info Window
        IPMapper.infowindow.close();
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
    placeIPMarkerFromJSON: function(json) {
        function place(data) {
            var latitude = data.latitude.toString();
            var longitude = data.longitude.toString();
            var contentString = "";
            $.each(data, function mark(key, val) {
                //mask session id
                if(key == "session_id" || key == 'polys')
                    contentString += '';
                else if(key == "identity"){
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> <a href="/identityiq/define/identity/identity.jsf?id='+val+'">'+data["user_name"]+'</a><br />';
                }
                else {
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> ' + val + '<br />';
                }
            });
            var latlng = new google.maps.LatLng(latitude, longitude);
            var ibanned = parseInt(data["banned"]);
            var marker = new google.maps.Marker({ //create Map Marker
                map: IPMapper.map,
                draggable: false,
                position: latlng,
                banned: ibanned,
                title: (data["user_name"]||data["IP"]),
                id: (data["ID"] || data["IP"]),
                polys: [],
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                        scale: 6.5,
                        fillColor: (ibanned >0) ? "#00" : "#F00",
                        fillOpacity: (ibanned > 0) ? 0.6 : 0.4,
                        strokeWeight: 0.5
                }
            });
            if(marker){
                allMarkers[data["ID"]] = marker;
                IPMapper.placeIPMarker(marker, latlng, contentString); //place Marker on Map
            }

        }
        var pairs= $.parseJSON(json);
        if (pairs && pairs.constructor === Array) {
            glob = pairs;
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
        };
        var icon2 = {
            path: google.maps.SymbolPath.CIRCLE,
            scale: 8.5,
            fillColor: "#66ff33",
            fillOpacity: 0.9,
            strokeWeight: 1
        };

        if(name == ''){
            // for(var x =0; x<allMarkers.length; x++){
            for(var x in allMarkers){
                allMarkers[x].setIcon(icon1);
            }

        }
        else {
            // for (var x = 0; x < allMarkers.length; x++) {
            for(var x in allMarkers){
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
};