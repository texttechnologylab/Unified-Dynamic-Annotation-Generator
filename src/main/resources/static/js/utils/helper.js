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

export { flatData, minOf, maxOf };
