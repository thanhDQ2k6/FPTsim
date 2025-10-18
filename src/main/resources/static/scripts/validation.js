document.addEventListener('DOMContentLoaded', function() {
    const signInForm = document.getElementById('signInForm');
    const signInErrorDiv = document.getElementById('signInError');
    const signInSuccessDiv = document.getElementById('signInSuccess');
    const signUpForm = document.getElementById('signUpForm');
    const signUpErrorDiv = document.getElementById('signUpError');

    // Function to display message and open modal
    function displayMessageAndOpenModal(messageDiv, modalId, message, isSuccess) {
        console.log(`Attempting to display message for modal: ${modalId}`);
        messageDiv.textContent = message;
        messageDiv.style.display = 'block';
        if (isSuccess) {
            messageDiv.classList.remove('text-error');
            messageDiv.classList.add('text-success');
        } else {
            messageDiv.classList.remove('text-success');
            messageDiv.classList.add('text-error');
        }
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            console.log(`Modal element found for ${modalId}. Calling showModal().`);
            modalElement.showModal();
        } else {
            console.error(`Modal element not found for ${modalId}.`);
        }
    }

    // Handle sign-in form submission
    if (signInForm) {
        signInForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            signInErrorDiv.style.display = 'none'; // Hide previous errors
            signInSuccessDiv.style.display = 'none'; // Hide previous success messages

            const formData = new URLSearchParams(new FormData(signInForm));
            try {
                const response = await fetch(signInForm.action, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData
                });
                const result = await response.json();

                if (result.success) {
                    displayMessageAndOpenModal(signInSuccessDiv, 'signIn', result.message, true);
                    setTimeout(() => {
                        window.location.href = '/cart'; // Redirect to cart after a short delay
                    }, 1500); // 1.5 seconds delay
                } else {
                    displayMessageAndOpenModal(signInErrorDiv, 'signIn', result.message, false);
                }
            } catch (error) {
                displayMessageAndOpenModal(signInErrorDiv, 'signIn', 'An unexpected error occurred.', false);
                console.error('Sign-in error:', error);
            }
        });
    }

            // Handle sign-up form submission
            if (signUpForm) {
                signUpForm.addEventListener('submit', async function(event) {
                    event.preventDefault();
                    signUpErrorDiv.style.display = 'none'; // Hide previous errors
    
                    const emailInput = signUpForm.querySelector('#email');
                    const email = emailInput.value;
    
                    // Client-side email validation (simpler)
                    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
                    if (!emailRegex.test(email)) {
                        displayMessageAndOpenModal(signUpErrorDiv, 'signUp', 'Email không hợp lệ!', false);
                        return;
                    }
    
                    const formData = new URLSearchParams(new FormData(signUpForm));
                    try {
                        const response = await fetch(signUpForm.action, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            body: formData
                        });
                        const result = await response.json();
    
                        if (result.success) {
                            alert(result.message); // Show success message
                            window.location.href = '/'; // Redirect to home page
                        } else {
                            displayMessageAndOpenModal(signUpErrorDiv, 'signUp', result.message, false);
                        }
                    } catch (error) {
                        displayMessageAndOpenModal(signUpErrorDiv, 'signUp', 'An unexpected error occurred.', false);
                        console.error('Sign-up error:', error);
                    }
                });
            }
    // Check for signup success parameter in URL and open sign-in modal
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('signupSuccess') && urlParams.get('signupSuccess') === 'true') {
        const signInModalElement = document.getElementById('signIn');
        if (signInModalElement) {
            console.log('Opening signIn modal due to signupSuccess parameter.');
            signInModalElement.showModal();
        } else {
            console.error('signIn modal element not found for signupSuccess parameter.');
        }
    }
});