function hideAllOtherActions(exceptId) {
    Array.from(document.getElementsByClassName("action"))
        .filter(element => element.id !== exceptId)
        .forEach(element => element.style.display = "none");
}

function toggleActionVisibility(id) {
    let element = document.getElementById(id);
    element.style.display = (element.style.display === "block") ? "none" : "block";
}

function registerShowActionOnClick(buttonId, actionId) {
    let element = document.getElementById(buttonId);
    if (!element) {
        return;
    }
    element.addEventListener("click", function() {
        hideAllOtherActions(actionId);
        toggleActionVisibility(actionId);
    })
}
