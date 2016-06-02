/*!
 * IP Address geocoding API for Google Maps
 * http://lab.abhinayrathore.com/ipmapper/
 * Last Updated: June 13, 2012
 */

var allMarkers = [];
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
        //info window close event
        google.maps.event.addListener(IPMapper.infowindow, 'closeclick', function() {
            IPMapper.map.fitBounds(IPMapper.latlngbound);
            IPMapper.map.panToBounds(IPMapper.latlngbound);
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
                console.log(json);
                // IPMapper.placeIPMarker(json['latitude'], json['longitude'], "{\"region_name\":"+ json['region_name'] + "\",city\":" + json['city']+ ",\"zip_code:\""+ json['zip_code']+"}");
                IPMapper.placeIPMarkerFromJSON(json);
                console.log("ADDING CUSTOM!!");
            })

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

    placeIPMarkerFromJSON: function(json) {
        function place(data) {
            var latitude = data.latitude;
            var longitude = data.longitude;
            var contentString = "";
            $.each(data, function (key, val) {
                if(key == "Identity Url"){
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> <a href="/identityiq/define/identity/identity.jsf?id='+val+'"> User Identity</a><br />';
                }
                else {
                    contentString += '<b>' + key.toUpperCase().replace("_", " ") + ':</b> ' + val + '<br />';
                }
            });
            var latlng = new google.maps.LatLng(latitude, longitude);
            var marker = new google.maps.Marker({ //create Map Marker
                map: IPMapper.map,
                draggable: false,
                position: latlng,
                title: (data["User Name"]||data["ip"]),
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                        scale: 6.5,
                        fillColor: "#F00",
                        fillOpacity: 0.4,
                        strokeWeight: 0.4
                }
            })
            // IPMapper.placeIPMarker(marker, latlng, contentString).done(allMarkers.push(marker)); //place Marker on Map
            IPMapper.placeIPMarker(marker, latlng, contentString); //place Marker on Map
            if(marker){
                allMarkers.push(marker);
                console.log("CONTENT IS " + contentString);
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
        for(var x =0; x<allMarkers.length; x++){
            if(allMarkers[x].title.search(new RegExp(name, "i")) > -1){
                console.log(allMarkers[x].name);
                var icon = {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 7.0,
                    fillColor: "#000000",
                    animation: google.maps.Animation.DROP,
                    fillOpacity: 0.7,
                    strokeWeight: 0.6
                }
                allMarkers[x].setIcon(icon);
            }
            else{
                var icon = {
                    path: google.maps.SymbolPath.CIRCLE,
                    scale: 6.5,
                    fillColor: "#F00",
                    fillOpacity: 0.4,
                    strokeWeight: 0.4
                }
                allMarkers[x].setIcon(icon);
            }
        }
    }
}