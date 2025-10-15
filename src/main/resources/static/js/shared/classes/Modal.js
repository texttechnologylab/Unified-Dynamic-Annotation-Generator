export default class Modal {
  constructor(element) {
    this.element = element;
    this.title = element.querySelector(".dv-title");
    this.body = element.querySelector(".dv-modal-body");
    this.buttons = element.querySelectorAll("button");
    this.listener;

    this.buttons[0].addEventListener("click", () => this.hide());
    this.buttons[1].addEventListener("click", () => this.hide());
  }

  confirm(title, message, onConfirm) {
    this.hide();

    this.title.textContent = title;
    this.body.textContent = message;
    this.listener = () => {
      onConfirm();
      this.hide();
    };
    this.buttons[2].addEventListener("click", this.listener);

    this.show();
  }

  alert(title, message) {
    this.hide();

    this.title.textContent = title;
    this.body.textContent = message;
    this.buttons[1].classList.add("dv-hidden");
    this.listener = () => this.hide();
    this.buttons[2].addEventListener("click", this.listener);

    this.show();
  }

  render(title, content, onConfirm) {
    this.hide();

    this.title.textContent = title;
    this.body.append(content);

    this.listener = () => {
      onConfirm();
      this.hide();
    };
    this.buttons[2].addEventListener("click", this.listener);

    this.show();
  }

  show() {
    this.element.classList.add("show");
    document.body.classList.add("modal-open");
  }

  hide() {
    this.title.innerHTML = "";
    this.body.innerHTML = "";
    this.buttons[1].classList.remove("dv-hidden");
    this.buttons[2].removeEventListener("click", this.listener);

    this.element.classList.remove("show");
    document.body.classList.remove("modal-open");
  }
}
