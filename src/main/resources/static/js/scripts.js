$(document).ready(function () {
    console.log("Page loaded successfully!");

    // Example AJAX call
    $("#exampleButton").click(function () {
        $.ajax({
            url: "/example-endpoint",
            method: "GET",
            success: function (data) {
                console.log("AJAX Request Success:", data);
            },
            error: function (error) {
                console.error("AJAX Request Failed:", error);
            }
        });
    });

    // Hide error message after 3 seconds
    setTimeout(function () {
        $("#messageAlert").fadeOut("slow");
    }, 3000);
});

// Add this to your scripts.js or in a <script> tag
$(document).ready(function () {
    const searchInput = $('#liveSearchInput');
    const resultsContainer = $('#searchResults');
    let timeoutId;

    // Debounce function to limit API calls
    function debounce(func, delay) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(func, delay);
    }

    searchInput.on('input', function () {
        const query = $(this).val().trim();

        if (query.length === 0) {
            resultsContainer.hide().empty();
            return;
        }

        console.log(query);
        debounce(() => {
            $.ajax({
                url: '/user/live-search',
                type: 'GET',
                data: {query: query},
                success: function (data) {
                    resultsContainer.html(data).show();
                },
                error: function () {
                    resultsContainer.html('<div class="alert alert-danger">Error loading results</div>').show();
                }
            });
        }, 300); // 300ms delay
    });

    // Hide results when clicking outside
    $(document).on('click', function (e) {
        if (!$(e.target).closest('#liveSearchInput, #searchResults').length) {
            resultsContainer.hide();
        }
    });
});

$(document).ready(function () {
    const passwordPattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*[\W]).{8,}$/;

    $('#changePasswordForm').on('submit', function (e) {
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();
        const errorDiv = $('#passwordError');

        let errorMessage = '';

        if (!passwordPattern.test(newPassword)) {
            errorMessage = 'Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, and one special character.';
        } else if (newPassword !== confirmPassword) {
            errorMessage = 'New password and confirmation do not match.';
        }

        if (errorMessage) {
            e.preventDefault();
            errorDiv.text(errorMessage).show();
        } else {
            errorDiv.hide();
        }
    });
});
