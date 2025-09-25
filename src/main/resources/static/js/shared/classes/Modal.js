import { createElement } from "../modules/utils.js";

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

  form(title, controls, onConfirm) {
    this.close();

    this.title.textContent = title;

    for (const item of controls) {
      const input = this.createInput(item);
      const label = createElement(
        "label",
        { className: "d-flex flex-column" },
        [item.label, input]
      );

      this.body.append(label);
    }

    this.listener = () => {
      const nodes = this.body.querySelectorAll("select, input");
      const values = Array.from(nodes).map((element) => element.value);
      onConfirm(...values);

      this.close();
    };
    this.buttons[2].addEventListener("click", this.listener);

    this.element.classList.add("show");
  }

  close() {
    this.title.innerHTML = "";
    this.body.innerHTML = "";
    this.buttons[1].classList.remove("dv-hidden");
    this.buttons[2].removeEventListener("click", this.listener);

    this.element.classList.remove("show");
  }

  createInput(item) {
    if (item.type === "select") {
      return createElement(
        "select",
        {
          className: "dv-select",
          value: item.value,
        },
        item.options.map((content) =>
          createElement("option", { textContent: content })
        )
      );
    } else {
      return createElement("input", {
        className: "dv-text-input",
        type: item.type,
        value: item.value,
      });
    }
  }
}
