function init(modal) {
  const form = document.querySelector("#file-upload");
  const dropArea = form.querySelector(".dv-file-drop-area");
  const fileInput = dropArea.querySelector("input[type='file']");

  ["dragenter", "dragover"].forEach((event) => {
    dropArea.addEventListener(event, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropArea.classList.add("dragover");
    });
  });

  ["dragleave", "drop"].forEach((event) => {
    dropArea.addEventListener(event, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropArea.classList.remove("dragover");
    });
  });

  dropArea.addEventListener("drop", (e) => {
    const files = e.dataTransfer.files;
    if (files.length === 1 && files[0].type === fileInput.accept) {
      fileInput.files = files;
      form.submit();
    } else {
      modal.alert("Invalid input", "Only a single json file is valid.");
    }
  });

  dropArea.addEventListener("click", () => fileInput.click());

  fileInput.addEventListener("change", () => {
    if (fileInput.files.length === 1) {
      form.submit();
    }
  });
}

export default { init };
