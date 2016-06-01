var results =  null;
function test () {
    console.log("starting getDB.......");
    results = getDB();
    for (var x = 0; x < results.length(); x++) {
        addElement(results[x]);
    }
}
//        });
test();
function addElement (content) {
    // create a new div element
    // and give it some content
    var newDiv = document.createElement("div");
    var newContent = document.createTextNode(content);
    newDiv.appendChild(newContent); //add the text node to the newly created div.

    // add the newly created element and its content into the DOM
    var currentDiv = document.getElementById("div1");
    document.body.insertBefore(newDiv, currentDiv);
    console.log("done???");
}
