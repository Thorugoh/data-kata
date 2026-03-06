#!/bin/bash

# Define output file
OUTPUT_FILE="ftp_data/sales_data.csv"

# Add CSV header
echo "sale_id,seller_id,city_id,sale_amount" > $OUTPUT_FILE

# Generate 1000 random sales records
for i in {1..1000}
do
   # Random seller between 1 and 5
   SELLER_ID=$(( ( RANDOM % 5 )  + 1 ))
   
   # Random city between 1 and 5
   CITY_ID=$(( ( RANDOM % 5 )  + 1 ))
   
   # Random sale amount between 10.00 and 500.00
   AMOUNT=$(awk -v min=10 -v max=500 'BEGIN{srand(); printf "%.2f\n", min+rand()*(max-min)}')
   
   echo "$i,$SELLER_ID,$CITY_ID,$AMOUNT" >> $OUTPUT_FILE
done

echo "Successfully generated $OUTPUT_FILE with 1000 records."