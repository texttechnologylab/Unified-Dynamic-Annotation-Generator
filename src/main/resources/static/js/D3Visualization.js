import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export default class AbstractChart {
  constructor(anchor, endpoint, margin, width, height) {
    this.anchor = anchor;
    this.endpoint = endpoint;
    this.margin = margin;
    this.width = width - this.margin.left - this.margin.right;
    this.height = height - this.margin.top - this.margin.bottom;

    this.tooltip = d3
      .select(this.anchor)
      .append("div")
      .attr("class", "tooltip");

    this.svg = d3
      .select(this.anchor)
      .append("svg")
      .attr("width", this.width + this.margin.left + this.margin.right)
      .attr("height", this.height + this.margin.top + this.margin.bottom)
      .append("g")
      .attr("transform", `translate(${this.margin.left}, ${this.margin.top})`);
  }

  async fetch() {
    const response = await fetch(this.endpoint);
    const result = await response.json();

    return result;
  }

  render() {
    throw new Error("Method render() not implemented.");
  }

  mouseover = (event) => {
    this.tooltip.style("opacity", 1);
    d3.select(event.currentTarget).style("opacity", 0.8);
  };

  mousemove = (event, data) => {
    const label = data.label || data.data?.label;
    const value = data.value || data.data?.value;

    this.tooltip
      .html(`<strong>${label}</strong><br>${value}`)
      .style("left", event.pageX + 20 + "px")
      .style("top", event.pageY + "px");
  };

  mouseleave = (event) => {
    this.tooltip.style("opacity", 0);
    d3.select(event.currentTarget).style("opacity", 1);
  };
}
