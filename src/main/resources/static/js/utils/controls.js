import { minOf, maxOf } from "./helper.js";

function appendSwitch(element, name, onchange) {
  element
    .append("div")
    .attr("class", "form-check form-switch")
    .append("label")
    .attr("class", "form-check-label")
    .text(name)
    .append("input")
    .attr("type", "checkbox")
    .attr("class", "form-check-input")
    .attr("value", name)
    .attr("checked", true)
    .on("change", (event) => onchange(event.target.value));
}

function appendSlider(element, min, max, onchange) {
  const values = [min, max];

  const label = element
    .append("label")
    .attr("class", "form-label w-100")
    .append("span")
    .text(`Range: ${min} - ${max}`);

  const container = element
    .select("label")
    .append("div")
    .attr("class", "position-relative");

  container
    .append("input")
    .attr("type", "range")
    .attr("class", "form-range dv-slider-double")
    .attr("min", min)
    .attr("max", max)
    .attr("value", max)
    .on("input", (event) => {
      values[1] = parseInt(event.target.value);
      label.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
    })
    .on("change", () => {
      onchange(minOf(values), maxOf(values));
    });

  container
    .append("input")
    .attr("type", "range")
    .attr("class", "form-range dv-slider-double")
    .attr("min", min)
    .attr("max", max)
    .attr("value", min)
    .on("input", (event) => {
      values[0] = parseInt(event.target.value);
      label.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
    })
    .on("change", () => {
      onchange(minOf(values), maxOf(values));
    });
}

export { appendSwitch, appendSlider };
