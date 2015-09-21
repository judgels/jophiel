requirejs.config({
    shim: {
        'jquery-textcomplete': {
            deps: ['jquery']
        }
    },
    paths: {
        'jquery-textcomplete': '/assets/lib/jquery-textcomplete/jquery.textcomplete.min'
    }
});

require(["jquery", "jquery-textcomplete"], function() {
    $(".client_multicomplete").textcomplete([
        {
            match: /\b(\w{2,})$/,
            search: function (term, callback) {
                $.ajax({
                    url: autocompleteClientAPIEndpoint + "?term=" + term,
                    type: 'GET',
                    dataType: "json",
                    success: function( data ) {
                        var processed = [];
                        for (var i=0;i<data.length;++i) {
                            processed.push(data[i].value);
                        }
                        callback(processed);
                    }
                });
            },
            index: 1,
            replace: function (res) {
                return res + ',';
            }
        }
    ]);
    $(".user_multicomplete").textcomplete([
        {
            match: /\b(\w{2,})$/,
            search: function (term, callback) {
                $.ajax({
                    url: autocompleteUserAPIEndpoint + "?term=" + term,
                    type: 'GET',
                    dataType: "jsonp",
                    success: function( data ) {
                        var processed = [];
                        for (var i=0;i<data.length;++i) {
                            processed.push(data[i].value);
                        }
                        callback(processed);
                    }
                });
            },
            index: 1,
            replace: function (res) {
                return res + ',';
            }
        }
    ]);
});
