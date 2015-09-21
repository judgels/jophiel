require(["jquery", "jquery-ui"], function() {
    $(".province_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: autocompleteProvinceAPIEndpoint,
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
