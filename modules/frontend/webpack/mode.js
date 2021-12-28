const DEVELOPMENT = "development";
const PRODUCTION = "production";

const currentMode = process.env.NODE_ENV || DEVELOPMENT;
const isDevelopment = currentMode === DEVELOPMENT;
const isProduction = currentMode === PRODUCTION;

console.info("Current mode: ", currentMode);

module.exports = {
  currentMode: currentMode,
  isDevelopment: isDevelopment,
  isProduction: isProduction,
};
