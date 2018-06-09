'use strict';
(function(){

    console.log("IPFS-STORE / Frontend test");

    const example = {
        "title": "My first Blog post",
        "content": "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        "author": "greg",
        "category": "Life",
        "date_created": "2018-01-01T20:00:00Z",
        "version": 1
    }

    $.ajax({ url: 'http://localhost:8040/ipfs-store/json/store', 
        type: 'POST',
        contentType: 'application/json', 
        data: JSON.stringify(example),
        success: function(res) {
            console.log(res);
        }
    });

    
})();