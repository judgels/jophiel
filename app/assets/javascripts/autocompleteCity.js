require(["jquery", "jquery-ui"], function() {
    $(".city_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: autocompleteCityAPIEndpoint,
                type: 'GET',
                data: {
                    term: request.term
                },
                dataType: "jsonp",
                success: function( data ) {
                    response( data );
                }
            });
        },
        minLength: 2
    });
});
