function flatData(datasets, key) {
  return datasets.flatMap(({ [key]: value, ...rest }) =>
    value.map((item) => ({ ...rest, ...item }))
  );
}

function minOf(array) {
  return Math.min(...array);
}

function maxOf(array) {
  return Math.max(...array);
}

function randomId(str) {
  return str + "-" + Math.random().toString(36).slice(2, 9);
}

function createElement(tag, attributes = {}, children = []) {
  const element = document.createElement(tag);
  Object.assign(element, attributes);
  element.append(...children);

  return element;
}

export { flatData, minOf, maxOf, randomId, createElement };
