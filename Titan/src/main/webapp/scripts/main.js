(function() {

    /**
     * Variables
     */
    var user_id = '';
    var user_fullname = '';
    var lng = -122.08;
    var lat = 37.38;

    /**
     * Initialize
     */
    function init() {
		$('login-btn').addEventListener('click', login);
        $('nearby-btn').addEventListener('click', loadNearbyItems);
        $('fav-btn').addEventListener('click', loadFavoriteItems);
        $('recommend-btn').addEventListener('click', loadRecommendedItems);

        // Event listeners for toggling between login and registration forms
        $('show-register').addEventListener('click', function(event) {
            event.preventDefault(); // Prevents the page from refreshing
            showRegisterForm(); // Function to show registration form
        });

        $('show-login').addEventListener('click', function(event) {
            event.preventDefault(); // Prevents the page from refreshing
            showLoginForm(); // Function to show login form
        });

        $('register-btn').addEventListener('click', registerUser); // Event listener for the register button

        validateSession(); // Validates the user session on load

        var welcomeMsg = $('welcome-msg');
        welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;
        initGeoLocation(); // Initializes geolocation
    }
	
	function showRegisterForm() {
        hideElement($('login-form')); // Hides the login form
        showElement($('register-form')); // Shows the registration form
        hideBottomSections(); // Hide bottom sections of the website
        hideElement($('main-section')); // Hide the yellow section when the form is active
    }

    function showLoginForm() {
        hideElement($('register-form')); // Hides the registration form
        showElement($('login-form')); // Shows the login form
        hideBottomSections(); // Hide bottom sections of the website
        hideElement($('main-section')); // Hide the yellow section when the form is active
    }

    function showBottomSections() {
        // Show all the bottom sections when user is logged in or registered
        showElement($('item-nav'));
        showElement($('item-list'));
        showElement(document.querySelector('footer'));
        showElement($('main-section')); // Show the yellow section again when logged in
    }

    function hideBottomSections() {
        // Hide all the bottom sections when showing login or registration forms
        hideElement($('item-nav'));
        hideElement($('item-list'));
        hideElement(document.querySelector('footer'));
    }
	
	/**
     * Handles the registration process
     */
    function registerUser() {
        // Get form values
        const userId = $('new-username').value.trim();
        const password = $('new-password').value.trim();
        const firstName = $('first-name').value.trim();
        const lastName = $('last-name').value.trim();

        // Basic validation
        if (!userId || !password || !firstName || !lastName) {
            $('register-error').textContent = 'All fields are required.';
            return;
        }

        // Hash password using MD5 if necessary before sending
        const hashedPassword = md5(userId + md5(password)); // Assuming md5 is available globally

        // Prepare request payload
        const payload = JSON.stringify({
            user_id: userId,
            password: hashedPassword,
            first_name: firstName,
            last_name: lastName
        });

        // Send AJAX request to the server
        fetch('./register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: payload
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'OK') {
                alert('Registration successful!');
                clearRegistrationForm();
                showLoginForm(); // Switch to login form
            } else {
                $('register-error').textContent = data.status || 'User creation failed. Please try again.';
            }
        })
        .catch(error => {
            console.error('Error during registration:', error);
            $('register-error').textContent = 'An error occurred. Please try again later.';
        });
    }
	
	/**
     * Clears the registration form fields and error message
     */
    function clearRegistrationForm() {
        $('new-username').value = '';
        $('new-password').value = '';
        $('first-name').value = '';
        $('last-name').value = '';
        $('register-error').textContent = '';
    }
	
	/**
	 * Session
	 */
	function validateSession() {
		// The request parameters
		var url = './login';
		var req = JSON.stringify({});

		// display loading message
		showLoadingMessage('Validating session...');

		// make AJAX call
		ajax('GET', url, req,
		// session is still valid
		function(res) {
			var result = JSON.parse(res);

			if (result.status === 'OK') {
				onSessionValid(result);
			}
		});
	}

	function onSessionValid(result) {
		user_id = result.user_id;
		user_fullname = result.name;

		var loginForm = $('login-form');
		var itemNav = $('item-nav');
		var itemList = $('item-list');
		var avatar = $('avatar');
		var welcomeMsg = $('welcome-msg');
		var logoutBtn = $('logout-link');

		welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;

		showElement(itemNav);
		showElement(itemList);
		showElement(avatar);
		showElement(welcomeMsg);
		showElement(logoutBtn, 'inline-block');
		hideElement(loginForm);

		initGeoLocation();
	}

	function onSessionInvalid() {
		var loginForm = $('login-form');
		var itemNav = $('item-nav');
		var itemList = $('item-list');
		var avatar = $('avatar');
		var welcomeMsg = $('welcome-msg');
		var logoutBtn = $('logout-link');

		hideElement(itemNav);
		hideElement(itemList);
		hideElement(avatar);
		hideElement(logoutBtn);
		hideElement(welcomeMsg);

		showElement(loginForm);
	}


    function initGeoLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onPositionUpdated,
                onLoadPositionFailed, {
                    maximumAge: 60000
                });
            showLoadingMessage('Retrieving your location...');
        } else {
            onLoadPositionFailed();
        }
    }

    function onPositionUpdated(position) {
        lat = position.coords.latitude;
        lng = position.coords.longitude;

        loadNearbyItems();
    }

    function onLoadPositionFailed() {
        console.warn('navigator.geolocation is not available');
        getLocationFromIP();
    }

    function getLocationFromIP() {
        // Get location from http://ipinfo.io/json
        var url = 'http://ipinfo.io/json'
        var req = null;
        ajax('GET', url, req, function(res) {
            var result = JSON.parse(res);
            if ('loc' in result) {
                var loc = result.loc.split(',');
                lat = loc[0];
                lng = loc[1];
            } else {
                console.warn('Getting location by IP failed.');
            }
            loadNearbyItems();
        });
    }
	
	// -----------------------------------
	// Login
	// -----------------------------------

	function login() {
		
		var username = $('username').value;
		var password = $('password').value;
		password = md5(username + md5(password));
		
		// The request parameters
		var url = './login';
		var req = JSON.stringify({
			user_id : username,
			password : password,
		});

		ajax('POST', url, req,
		// successful callback
		function(res) {
			var result = JSON.parse(res);

			// successfully logged in
			if (result.status === 'OK') {
				onSessionValid(result);
			}
		},

		// error
		function() {
			showLoginError();
		});
	}

	function showLoginError() {
		$('login-error').innerHTML = 'Invalid username or password';
	}

	function clearLoginError() {
		$('login-error').innerHTML = '';
	}



    // -----------------------------------
    // Helper Functions
    // -----------------------------------

    /**
     * A helper function that makes a navigation button active
     * 
     * @param btnId -
     *            The id of the navigation button
     */
    function activeBtn(btnId) {
        var btns = document.getElementsByClassName('main-nav-btn');

        // deactivate all navigation buttons
        for (var i = 0; i < btns.length; i++) {
            btns[i].className = btns[i].className.replace(/\bactive\b/, '');
        }

        // active the one that has id = btnId
        var btn = $(btnId);
        btn.className += ' active';
    }

    function showLoadingMessage(msg) {
        var itemList = $('item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' +
            msg + '</p>';
    }

    function showWarningMessage(msg) {
        var itemList = $('item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' +
            msg + '</p>';
    }

    function showErrorMessage(msg) {
        var itemList = $('item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' +
            msg + '</p>';
    }

    /**
     * A helper function that creates a DOM element <tag options...>
     * 
     * @param tag
     * @param options
     * @returns
     */
    function $(tag, options) {
        if (!options) {
            return document.getElementById(tag);
        }

        var element = document.createElement(tag);

        for (var option in options) {
            if (options.hasOwnProperty(option)) {
                element[option] = options[option];
            }
        }

        return element;
    }

    function hideElement(element) {
        element.style.display = 'none';
    }

    function showElement(element, style) {
        var displayStyle = style ? style : 'block';
        element.style.display = displayStyle;
    }

    /**
     * AJAX helper
     * 
     * @param method -
     *            GET|POST|PUT|DELETE
     * @param url -
     *            API end point
     * @param callback -
     *            This the successful callback
     * @param errorHandler -
     *            This is the failed callback
     */
    function ajax(method, url, data, callback, errorHandler) {
        var xhr = new XMLHttpRequest();

        xhr.open(method, url, true);

        xhr.onload = function() {
			if (xhr.status === 200) {
				callback(xhr.responseText);
			} else if (xhr.status === 403) {
				onSessionInvalid();
			} else {
				errorHandler();
			}
        };

        xhr.onerror = function() {
            console.error("The request couldn't be completed.");
            errorHandler();
        };

        if (data === null) {
            xhr.send();
        } else {
            xhr.setRequestHeader("Content-Type",
                "application/json;charset=utf-8");
            xhr.send(data);
        }
    }

    // -------------------------------------
    // AJAX call server-side APIs
    // -------------------------------------

    /**
     * API #1 Load the nearby items API end point: [GET]
     * /Dashi/search?user_id=1111&lat=37.38&lon=-122.08
     */
    function loadNearbyItems() {
        console.log('loadNearbyItems');
        activeBtn('nearby-btn');

        // The request parameters
        var url = './search';
        var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading nearby items...');

        // make AJAX call
        ajax('GET', url + '?' + params, req,
            // successful callback
            function(res) {
                var items = JSON.parse(res);
                if (!items || items.length === 0) {
                    showWarningMessage('No nearby item.');
                } else {
                    listItems(items);
                }
            },
            // failed callback
            function() {
                showErrorMessage('Cannot load nearby items.');
            });
    }

    /**
     * API #2 Load favorite (or visited) items API end point: [GET]
     * /Dashi/history?user_id=1111
     */
    function loadFavoriteItems() {
        activeBtn('fav-btn');

        // The request parameters
        var url = './history';
        var params = 'user_id=' + user_id;
        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading favorite items...');

        // make AJAX call
        ajax('GET', url + '?' + params, req, function(res) {
            var items = JSON.parse(res);
            if (!items || items.length === 0) {
                showWarningMessage('No favorite item.');
            } else {
                listItems(items);
            }
        }, function() {
            showErrorMessage('Cannot load favorite items.');
        });
    }

    /**
     * API #3 Load recommended items API end point: [GET]
     * /Dashi/recommendation?user_id=1111
     */
    function loadRecommendedItems() {
        activeBtn('recommend-btn');

        // The request parameters
        var url = './recommendation';
        var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;

        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading recommended items...');

        // make AJAX call
        ajax(
            'GET',
            url + '?' + params,
            req,
            // successful callback
            function(res) {
                var items = JSON.parse(res);
                if (!items || items.length === 0) {
                    showWarningMessage('No recommended item. Make sure you have favorites.');
                } else {
                    listItems(items);
                }
            },
            // failed callback
            function() {
                showErrorMessage('Cannot load recommended items.');
            });
    }

    /**
     * API #4 Toggle favorite (or visited) items
     * 
     * @param item_id -
     *            The item business id
     * 
     * API end point: [POST]/[DELETE] /Dashi/history request json data: {
     * user_id: 1111, visited: [a_list_of_business_ids] }
     */
    function changeFavoriteItem(item_id) {
        // Check whether this item has been visited or not
        var li = $('item-' + item_id);
        var favIcon = $('fav-icon-' + item_id);
        var favorite = li.dataset.favorite !== 'true';

        // The request parameters
        var url = './history';
        var req = JSON.stringify({
            user_id: user_id,
            favorite: [item_id]
        });
        var method = favorite ? 'POST' : 'DELETE';

        ajax(method, url, req,
            // successful callback
            function(res) {
                var result = JSON.parse(res);
                if (result.result === 'SUCCESS') {
                    li.dataset.favorite = favorite;
                    favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
                }
            });
    }

    // -------------------------------------
    // Create item list
    // -------------------------------------

    /**
     * List items
     * 
     * @param items -
     *            An array of item JSON objects
     */
    function listItems(items) {
        // Clear the current results
        var itemList = $('item-list');
        itemList.innerHTML = '';

        for (var i = 0; i < items.length; i++) {
            addItem(itemList, items[i]);
        }
    }

    /**
     * Add item to the list
     * 
     * @param itemList -
     *            The
     *            <ul id="item-list">
     *            tag
     * @param item -
     *            The item data (JSON object)
     */
    function addItem(itemList, item) {
        var item_id = item.item_id;

        // create the <li> tag and specify the id and class attributes
        var li = $('li', {
            id: 'item-' + item_id,
            className: 'item'
        });

        // set the data attribute
        li.dataset.item_id = item_id;
        li.dataset.favorite = item.favorite;

        // item image
        if (item.image_url) {
            li.appendChild($('img', {
                src: item.image_url
            }));
        } else {
            li.appendChild($(
                'img', {
                    src: 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
                }))
        }
        // section
        var section = $('div', {});

        // title
        var title = $('a', {
            href: item.url,
            target: '_blank',
            className: 'item-name'
        });
        title.innerHTML = item.name;
        section.appendChild(title);

        // category
        var category = $('p', {
            className: 'item-category'
        });
        category.innerHTML = 'Category: ' + item.categories.join(', ');
        section.appendChild(category);

        // TODO(vincent). here we might have a problem showing 3.5 as 3.
        // stars
        var stars = $('div', {
            className: 'stars'
        });

        for (var i = 0; i < item.rating; i++) {
            var star = $('i', {
                className: 'fa fa-star'
            });
            stars.appendChild(star);
        }

        if (('' + item.rating).match(/\.5$/)) {
            stars.appendChild($('i', {
                className: 'fa fa-star-half-o'
            }));
        }

        section.appendChild(stars);

        li.appendChild(section);

        // address
        var address = $('p', {
            className: 'item-address'
        });

        address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g,
            '');
        li.appendChild(address);

        // favorite link
        var favLink = $('p', {
            className: 'fav-link'
        });

        favLink.onclick = function() {
            changeFavoriteItem(item_id);
        };

        favLink.appendChild($('i', {
            id: 'fav-icon-' + item_id,
            className: item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
        }));

        li.appendChild(favLink);

        itemList.appendChild(li);
    }

    init();

})();
