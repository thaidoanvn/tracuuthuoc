const String apiBaseUrl = 'https://tracuuthuoc-production.up.railway.app';

// Release builds override this with --dart-define=API_KEY=....
const String apiKey = String.fromEnvironment(
  'API_KEY',
  defaultValue: 'thaidoan-tracuuthuoc-local',
);
