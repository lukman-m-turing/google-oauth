let playlistTitle = sessionStorage.getItem("playlistTitle");
const h2 = document.getElementById("heading");
h2.innerText = playlistTitle;

let playlistId = getPlaylistId();

function getPlaylistId() {
    var url = window.location.search.substring(1);
    var queryParams = url.split('&');
    for (var i = 0; i < queryParams.length; i++)
    {
        var queryParam = queryParams[i].split('=');
        if (queryParam[0] == "playlistId")
        {
            return queryParam[1];
        }
    }
}

const accessToken = sessionStorage.getItem("access-token");

if (accessToken != null && playlistId != null) {
    const videosUrl = "http://localhost:8080/auth/youtube/playlists/" + playlistId + "/videos";
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            const videos = JSON.parse(xmlHttp.responseText);
            for (let i = 0; i < videos.length; i++) {
                let video = {
                    id: videos[i].id,
                    videoId: videos[i].videoId,
                    title: videos[i].title,
                    thumbnail: videos[i].thumbnail,
                    description: videos[i].description
                };

                createVideoCard(video);
            }
        }
    }
    xmlHttp.open("GET", videosUrl, true);
    xmlHttp.setRequestHeader("Authorization", accessToken);
    xmlHttp.send(null);
}

function createVideoCard(video) {
    let imageView = document.createElement("img");
    imageView.src = video.thumbnail;
    imageView.alt = "thumbnail";
    imageView.style.borderTopLeftRadius = "6px";
    imageView.style.borderTopRightRadius = "6px";
    imageView.style.borderBottomRightRadius = "0";
    imageView.style.borderBottomLeftRadius = "0";
    imageView.style.width = "100%";
    imageView.style.height = "100%";

    let heading = document.createElement("h4");
    let titleBold = document.createElement("b");
    titleBold.innerText = video.title;
    heading.appendChild(titleBold);
    let paragraph = document.createElement("p");
    let description = video.description;
    if (description.length > 70) {
        description = description.substring(0, 70) + "...";
    }
    paragraph.innerText = description;
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

    let cardContainer = document.getElementById("videos");
    cardContainer.appendChild(card);

    card.addEventListener("mouseover", function () {
        card.style.cursor = "pointer";
        card.style.boxShadow = "0 8px 16px 0 rgba(0,0,0,0.2)";
    });
    card.addEventListener("mouseout", function () {
        card.style.boxShadow = "0 4px 8px 0 rgba(0,0,0,0.2)";
    });
    card.addEventListener("click", function () {
        let videoId = video.videoId;
        let videoUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=1";
        let previousPlayer = document.getElementById("youtubePlayer");
        if (previousPlayer != null) {
            previousPlayer.parentNode.removeChild(previousPlayer);
        }
        let videoPlayerFrame = document.createElement("iframe");
        videoPlayerFrame.setAttribute("allow", "fullscreen");
        videoPlayerFrame.id = "youtubePlayer";
        videoPlayerFrame.width = "420";
        videoPlayerFrame.height = "315";
        videoPlayerFrame.src = videoUrl;
        cardContainer.insertBefore(videoPlayerFrame, cardContainer.firstChild)
        window.scrollTo(0, 0); //scroll to top after video begins (just in case)
    });
}