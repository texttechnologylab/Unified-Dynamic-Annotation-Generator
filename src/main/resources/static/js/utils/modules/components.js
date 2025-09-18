import {
  Dropdown,
  Collapse,
} from "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/+esm";

function initSidepanels() {
  document.querySelectorAll("[data-dv-toggle='sidepanel']").forEach((node) => {
    node.addEventListener("click", () => {
      const target = document.querySelector(node.dataset.dvTarget);
      target.classList.toggle("show");
    });
  });

  document.querySelectorAll("[data-dv-dismiss='sidepanel']").forEach((node) => {
    node.addEventListener("click", () => {
      const target = node.closest(".dv-sidepanel");
      target.classList.remove("show");
    });
  });
}

function initDropdowns() {
  document.querySelectorAll("[data-bs-toggle='dropdown']").forEach((node) => {
    new Dropdown(node);
  });
}

function initAccordions() {
  document.querySelectorAll("[data-dv-toggle='accordion']").forEach((node) => {
    const target = document.querySelector(node.dataset.dvTarget);
    const collapse = new Collapse(target, { toggle: false });
    const chevron = node.querySelector(".bi-chevron-down");

    node.addEventListener("click", () => {
      collapse.toggle();
    });
    target.addEventListener("show.bs.collapse", () => {
      chevron.classList.add("rotate");
    });
    target.addEventListener("hide.bs.collapse", () => {
      chevron.classList.remove("rotate");
    });
  });
}

function initModal(modal) {
  document.querySelectorAll("[data-dv-toggle='modal']").forEach((node) => {
    node.addEventListener("click", () => {
      modal.confirm(
        "Delete " + node.dataset.pipeline,
        "Do you want to delete this pipeline?",
        () => console.log(node.dataset.pipeline)
      );
    });
  });
}

function initFileInput(modal) {
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

export default {
  initSidepanels,
  initDropdowns,
  initAccordions,
  initModal,
  initFileInput,
};
