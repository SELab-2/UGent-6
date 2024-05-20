ENV_FILE=".env"

while IFS= read -r line
do
  echo "Processing line: $line"
  IFS=',' read -r full_addr var <<< "$line"
  IFS='=' read -r file env <<< "$full_addr"
  echo "File: $file"
  echo "Variable: $var"
  echo "Value: $env"
  touch "$file"
  if ! grep -q "${var}=" "$file"; then
    echo "Variable not set, appending to file..."
    echo "${var}=${env}" >> "$file"
  else
    echo "Variable already set in file."
  fi
done < "$ENV_FILE"