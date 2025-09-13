export default class Modal {
  constructor(element) {
    this.element = element;
    this.title = element.querySelector(".dv-title");
    this.body = element.querySelector(".dv-modal-body");
    this.buttons = element.querySelectorAll("button");
    this.listener;

    this.buttons[0].addEventListener("click", () => this.close());
    this.buttons[1].addEventListener("click", () => this.close());
  }

  confirm(title, message, onConfirm) {
    this.close();

    this.title.textContent = title;
    this.body.textContent = message;
    this.listener = () => {
      onConfirm();
      this.close();
    };
    this.buttons[2].addEventListener("click", this.listener);

    this.element.classList.add("show");
  }

  alert(title, message) {
    this.close();

    this.title.textContent = title;
    this.body.textContent = message;
    this.buttons[1].classList.add("dv-hidden");
    this.listener = () => this.close();
    this.buttons[2].addEventListener("click", this.listener);

    this.element.classList.add("show");
  }

  close() {
    this.title.textContent = "";
    this.body.textContent = "";
    this.buttons[1].classList.remove("dv-hidden");
    this.buttons[2].removeEventListener("click", this.listener);

    this.element.classList.remove("show");
  }
}
