import { minOf, maxOf, randomId } from "./helper.js";

export default class ControlsHandler {
  constructor(node, icons) {
    this.node = node;
    this.icons = {
      asc: "bi bi-sort-up",
      desc: "bi bi-sort-down",
      includes: "bi bi-braces-asterisk",
      regex: "bi bi-regex",
      ...icons,
    };
  }

  appendInputRadio(title, radios, onchange) {
    const values = ["", radios[0]];
    const name = randomId(radios.join("-"));

    this.node.append("label").attr("class", "form-label").text(title);

    const group = this.node
      .append("div")
      .attr("class", "input-group input-group-sm mb-3");

    // Append text input
    group
      .append("input")
      .attr("class", "form-control")
      .attr("type", "text")
      .attr("placeholder", "type something")
      .on("change", (event) => {
        const input = event.target.value.trim();
        if (input !== "") {
          values[0] = input;
          onchange(...values);
        }
      });

    // Append radio buttons
    radios.forEach((radio, index) => {
      group
        .append("input")
        .attr("type", "radio")
        .attr("id", name + index)
        .attr("class", "btn-check")
        .attr("name", name)
        .property("checked", index === 0)
        .on("change", () => {
          values[1] = radio;
          onchange(...values);
        });
      // Append icon
      group
        .append("label")
        .attr("for", name + index)
        .attr("title", radio)
        .attr("class", "btn btn-outline-primary")
        .append("i")
        .attr("class", this.icons[radio]);
    });
  }

  appendSelectRadio(title, options, radios, onchange) {
    const values = [options[0], radios[0]];
    const name = randomId(radios.join("-"));

    this.node.append("label").attr("class", "form-label").text(title);

    const group = this.node
      .append("div")
      .attr("class", "input-group input-group-sm mb-3");

    // Append select
    const select = group
      .append("select")
      .attr("class", "form-select")
      .on("change", (event) => {
        values[0] = event.target.value;
        onchange(...values);
      });
    options.forEach((option) => select.append("option").text(option));

    // Append radio buttons
    radios.forEach((radio, index) => {
      group
        .append("input")
        .attr("type", "radio")
        .attr("id", name + index)
        .attr("class", "btn-check")
        .attr("name", name)
        .property("checked", index === 0)
        .on("change", () => {
          values[1] = radio;
          onchange(...values);
        });
      // Append icon
      group
        .append("label")
        .attr("for", name + index)
        .attr("title", radio)
        .attr("class", "btn btn-outline-primary")
        .append("i")
        .attr("class", this.icons[radio]);
    });
  }

  appendSwitch(name, onchange) {
    this.node
      .append("div")
      .attr("class", "form-check form-switch")
      .append("label")
      .attr("class", "form-check-label")
      .text(name)
      .append("input")
      .attr("type", "checkbox")
      .attr("class", "form-check-input")
      .attr("value", name)
      .property("checked", true)
      .on("change", (event) => onchange(event.target.value));
  }

  appendDoubleSlider(min, max, onchange) {
    const values = [min, max];

    const label = this.node.append("label").attr("class", "form-label w-100");

    const span = label.append("span").text(`Range: ${min} - ${max}`);
    const container = label.append("div").attr("class", "position-relative");

    // Append first slider
    container
      .append("input")
      .attr("type", "range")
      .attr("class", "form-range dv-slider-double")
      .attr("min", min)
      .attr("max", max)
      .attr("value", max)
      .on("input", (event) => {
        values[1] = parseInt(event.target.value);
        span.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
      })
      .on("change", () => {
        onchange(minOf(values), maxOf(values));
      });

    // Append second slider
    container
      .append("input")
      .attr("type", "range")
      .attr("class", "form-range dv-slider-double")
      .attr("min", min)
      .attr("max", max)
      .attr("value", min)
      .on("input", (event) => {
        values[0] = parseInt(event.target.value);
        span.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
      })
      .on("change", () => {
        onchange(minOf(values), maxOf(values));
      });
  }
}
