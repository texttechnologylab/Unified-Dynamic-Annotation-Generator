import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export default class D3Visualization {
  constructor(root, endpoint, margin, width, height) {
    this.root = d3.select(root);
    this.endpoint = endpoint;
    this.margin = margin;
    this.width = width - this.margin.left - this.margin.right;
    this.height = height - this.margin.top - this.margin.bottom;

    this.tooltip = this.root.select(".dv-tooltip");
    this.controls = this.root.select(".dv-sidepanel-body");

    // Add svg
    this.svg = this.root
      .select(".dv-chart-area")
      .append("svg")
      .attr("width", this.width + this.margin.left + this.margin.right)
      .attr("height", this.height + this.margin.top + this.margin.bottom)
      .append("g")
      .attr("transform", `translate(${this.margin.left}, ${this.margin.top})`);
  }

  async fetch(params = {}) {
    const url = new URL(this.endpoint);

    for (const [key, value] of Object.entries(params)) {
      url.searchParams.append(key, value);
    }

    const result = await fetch(url).then((response) => response.json());

    return result;
  }

  clear() {
    this.svg.selectAll("*").remove();
  }

  render() {
    throw new Error("Method render() not implemented.");
  }

  mouseover = (event) => {
    this.tooltip.style("opacity", 0.96);
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
