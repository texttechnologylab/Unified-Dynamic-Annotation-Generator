import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class LineChartHandler extends FormHandler {
  static defaults = {
    type: "LineChart",
    title: "Line Chart",
    generator: { id: "" },
    options: {
      line: true,
      dots: true,
    },
    icon: "bi bi-graph-up",
    w: 4,
    h: 3,
  };

  constructor(element, item) {
    super(element);

    this.item = item;
    this.span = this.element.querySelector("span");
  }

  init(modal, grid) {
    this.span.textContent = this.item.title;
    this.setIcon(this.item.icon);

    this.initButtons(modal, "Line Chart Options", grid);
  }

  createForm() {
    const titleInput = this.createTextInput("title", "Title", this.item.title);
    const generatorInput = this.createTextInput(
      "generator",
      "Generator",
      this.item.generator.id
    );
    const lineInput = this.createSelect(
      "line",
      "Draw lines",
      ["yes", "no"],
      this.item.options.line ? "yes" : "no"
    );
    const dotsInput = this.createSelect(
      "dots",
      "Draw dots",
      ["yes", "no"],
      this.item.options.dots ? "yes" : "no"
    );

    return createElement("form", { className: "dv-form-column" }, [
      titleInput,
      generatorInput,
      lineInput,
      dotsInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.title = data.title;
    this.item.generator.id = data.generator;
    this.item.options.line = data.line === "yes";
    this.item.options.dots = data.dots === "yes";

    // Update Title
    this.span.textContent = data.title;
  }
}
