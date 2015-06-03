require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        var source = $(".match-src");
        var target = $(".match-tgt");
        var passwordVal = "";
        source.keyup(function () {
            var val = source.val();
            if (passwordVal !== val) {
                passwordVal = val;
                target.val("");
                target.nextAll().remove();
            }
        });
        target.keyup(function () {
            $(this).nextAll().remove();
            var val = $(this).val();
            if ((val !== "") && (val !== passwordVal)) {
                var result = zxcvbn(val);
                $(this).after("<span class=\"label label-danger\">Not Match</span>");
            }
        });
    });
});
