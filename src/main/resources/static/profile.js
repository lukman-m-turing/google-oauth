var token = getTokenFromUrl();

function getTokenFromUrl()
{
    let accessToken = sessionStorage.getItem("access-token");
    if (accessToken !== null) {
        return accessToken;
    }

    var url = window.location.search.substring(1);
    var queryParams = url.split('&');
    for (var i = 0; i < queryParams.length; i++)
    {
        var queryParam = queryParams[i].split('=');
        if (queryParam[0] == "token")
        {
            return queryParam[1];
        }
    }
}

getUserProfileAsync(token);

function getUserProfileAsync(token) {
    const userProfileUrl = "http://localhost:8080/users";
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            initSession(token);
            displayUserInfo(xmlHttp.responseText);
        }
    }
    xmlHttp.open("GET", userProfileUrl, true);
    xmlHttp.setRequestHeader("Authorization", token);
    xmlHttp.send(null);
    window.history.replaceState({}, "Profile Page",
        window.location.href.split('?')[0]); //clear token from url
}

function displayUserInfo (user) {
    const userJson = JSON.parse(user);
    const profilePictureUrl = userJson.profilePicture;
    const fullName = userJson.firstName + " " + userJson.lastName;
    const email = userJson.email;
    const verified = userJson.verified === true ? "Verified" : "Not Verified";

    var pictureElement = document.getElementById("profile-picture");
    pictureElement.setAttribute("src", profilePictureUrl);

    var nameElement = document.getElementById("full-name");
    nameElement.appendChild(document.createTextNode(fullName))

    var emailElement = document.getElementById("user-email");
    emailElement.appendChild(document.createTextNode(email));

    var verifiedElement = document.getElementById("verified");
    if (verified.valueOf() === "Verified") {
        verifiedElement.style.color = "#009900"; //green
    } else {
        verifiedElement.hidden = true;
    }
    verifiedElement.appendChild(document.createTextNode(verified));

}

function initSession(token) {
    sessionStorage.setItem("access-token", token);
}

function connectYoutube(playlistsUrl) {
    window.location.href = playlistsUrl;
}

getUsersStatistics();
function getUsersStatistics() {
    const userStatisticsUrl = "http://localhost:8080/users/statistics";
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            displayUserStatistics(JSON.parse(xmlHttp.responseText));
        } else if ((xmlHttp.readyState == 4 && xmlHttp.status >= 400)) {
            hideStatisticsTable();
        }
    }
    xmlHttp.open("GET", userStatisticsUrl, true);
    xmlHttp.setRequestHeader("Authorization", token);
    xmlHttp.send(null);
}

function displayUserStatistics(statistics) {
    document.getElementById("totalUsers").innerText = statistics.totalNumberOfUsers;
    document.getElementById("totalVerified").innerText = statistics.totalNumberVerified;
    document.getElementById("totalOnYoutube").innerText = statistics.totalConnectedToYoutube;
}

function hideStatisticsTable() {
    document.getElementById("users-statistics").hidden = true;
}