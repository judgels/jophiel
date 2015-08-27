require(["jquery", "jquery-ui"], function( __tes__ ) {
    $(".institution_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: institutionAutoCompleteUrl,
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