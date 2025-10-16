import { createElement } from "../../../shared/modules/utils.js";

export default class FormHandler {
  static defaults = {};

  constructor(element) {
    this.element = element;
  }

  init() {
    throw new Error("Method init() not implemented.");
  }

  createForm() {
    throw new Error("Method createForm() not implemented.");
  }

  saveForm() {
    throw new Error("Method saveForm() not implemented.");
  }

  setIcon(icon) {
    this.element.querySelector(".dv-chart-area>i").className = icon;
  }

  initButtons(modal, modalTitle, grid) {
    const buttons = this.element.querySelectorAll("button");

    buttons[0].addEventListener("click", () => {
      const form = this.createForm();
      modal.render(modalTitle, form, () => this.saveForm(form));
    });
    buttons[1].addEventListener("click", () => grid.removeWidget(this.item.el));
  }

  createTextInput(key, title, value) {
    const input = createElement("input", {
      name: key,
      type: "text",
      value,
      className: "dv-text-input",
    });
    const label = createElement("label", { className: "d-flex flex-column" }, [
      createElement("span", { textContent: title }),
      input,
    ]);

    return label;
  }

  createNumberInput(key, title, value, min, max) {
    const input = createElement("input", {
      name: key,
      type: "number",
      value,
      min,
      max,
      className: "dv-text-input",
    });
    const label = createElement("label", { className: "d-flex flex-column" }, [
      createElement("span", { textContent: title }),
      input,
    ]);

    return label;
  }

  createSelect(key, title, options, selected) {
    const select = createElement(
      "select",
      {
        name: key,
        className: "dv-select",
      },
      options.map((opt) =>
        createElement("option", {
          textContent: opt,
          value: opt,
          selected: opt === selected,
        })
      )
    );

    const label = createElement("label", { className: "d-flex flex-column" }, [
      createElement("span", { textContent: title }),
      select,
    ]);

    return label;
  }
}
