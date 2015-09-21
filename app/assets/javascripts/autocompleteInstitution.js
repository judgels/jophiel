require(["jquery", "jquery-ui"], function() {
    $(".institution_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: autocompleteInstitutionAPIEndpoint,
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
