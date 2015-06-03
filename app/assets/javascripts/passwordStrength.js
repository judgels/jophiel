requirejs.config({
    paths: {
        'zxcvbn': '/assets/lib/zxcvbn/zxcvbn'
    }
});

require(["jquery", "zxcvbn"], function( __tes__ ) {
    $(document).ready(function() {
        var strengthLabel = ["Very Weak", "Weak", "Moderate", "Strong", "Very Strong"];
        var bootstrapStatusClasses = ["danger", "danger", "warning", "success", "success"];
        $(".zxcvbn").keyup(function () {
            $(this).nextAll().remove();
            var val = $(this).val();
            if (val !== "") {
                var result = zxcvbn(val);
                $(this).after("<span class=\"label label-" + bootstrapStatusClasses[result.score] + "\">" + strengthLabel[result.score] + "</span>");
            }
        });
    });
});
