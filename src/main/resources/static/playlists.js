var token = getAccessToken();
sessionStorage.setItem("access-token", token);
window.history.replaceState({}, "Why Tube",
    window.location.href.split('?')[0]); //clear token from url

function getAccessToken()
{
    let accessToken = sessionStorage.getItem("access-token");
    if (accessToken !== "undefined") {
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

if (token != null) {
    const playlistsUrl = "http://localhost:8080/auth/youtube/playlists";
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            const playlists = JSON.parse(xmlHttp.responseText);
            for (let i = 0; i < playlists.length; i++) {
                let playlist = {
                    id: playlists[i].id,
                    title: playlists[i].title,
                    thumbnail: playlists[i].thumbnail,
                    numberOfVideos: playlists[i].numberOfVideos
                };

                createPlaylistCard(playlist);
            }
        }
    }
    xmlHttp.open("GET", playlistsUrl, true);
    xmlHttp.setRequestHeader("Authorization", token);
    xmlHttp.send(null);
} else {
    //TODO: redirect to authorization server
}

function createPlaylistCard(playlist) {
    let imageView = document.createElement("img");
    imageView.src = playlist.thumbnail;
    imageView.alt = "thumbnail";
    imageView.style.borderTopLeftRadius = "6px";
    imageView.style.borderTopRightRadius = "6px";
    imageView.style.borderBottomRightRadius = "0";
    imageView.style.borderBottomLeftRadius = "0";
    imageView.style.width = "100%";
    imageView.style.height = "100%";

    let heading = document.createElement("h4");
    let titleBold = document.createElement("b");
    titleBold.innerText = playlist.title;
    heading.appendChild(titleBold);
    let paragraph = document.createElement("p");
    paragraph.innerText = playlist.numberOfVideos + " videos";
    let container = document.createElement("container");
    container.className = "container";
    container.style.paddingTop = "2px";
    container.style.paddingBottom = "2px";
    container.style.paddingRight = "16px";
    container.style.paddingLeft = "16px";
    container.appendChild(heading);
    container.appendChild(paragraph);

    let card = document.createElement("div");
    card.style.display = "inline-block";
    card.style.boxShadow = "0 4px 8px 0 rgba(0,0,0,0.2)";
    card.style.width = "250px";
    card.style.transition = "0.3s";
    card.style.borderRadius = "5px"
    card.style.padding = "10px";
    card.style.margin = "14px";
    card.appendChild(imageView);
    card.appendChild(container);

    let cardContainer = document.getElementById("playlists");
    cardContainer.appendChild(card);

    card.addEventListener("mouseover", function () {
        card.style.cursor = "pointer";
        card.style.boxShadow = "0 8px 16px 0 rgba(0,0,0,0.2)";
    });
    card.addEventListener("mouseout", function () {
        card.style.boxShadow = "0 4px 8px 0 rgba(0,0,0,0.2)";
    });
    card.addEventListener("click", function () {
        sessionStorage.setItem("playlistTitle", playlist.title)
        loadPlaylist(playlist.id);
    });
}

function loadPlaylist(playlistId) {
    document.location.href = "http://localhost:8080/videos.html?playlistId=" + playlistId;
}