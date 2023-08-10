package org.mule.extension.parquet.internal.operation;

import static org.mule.extension.parquet.internal.io.OutputFile.nioPathToOutputFile;

import org.mule.extension.parquet.internal.ParquetBufferedReader;
import org.mule.extension.parquet.internal.ParquetBufferedWriter;
import org.mule.extension.parquet.internal.int96.ParquetTimestampUtils;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.FILE;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData.Record;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.tools.json.JsonRecordFormatter;
import org.apache.parquet.tools.read.SimpleReadSupport;
import org.apache.parquet.tools.read.SimpleRecord;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import org.apache.avro.data.TimeConversions;

import org.apache.avro.Conversion;
import org.apache.avro.io.BinaryEncoder;

import org.apache.avro.io.DatumReader;

import java.time.Instant;
import org.apache.avro.LogicalType;

import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import static org.mule.runtime.extension.api.annotation.param.Optional.PAYLOAD;

public class ParquetOperations {

	@MediaType(value = ANY, strict = false)
	@DisplayName("Write Avro to Parquet - Stream")
	public InputStream writeAvroToParquetStream(@Optional(defaultValue = PAYLOAD) InputStream body,
			@Optional(defaultValue = "UNCOMPRESSED") @DisplayName("Compression Codec") CompressionCodecName codec)
			throws IOException {

		GenericDatumReader<Object> greader = new GenericDatumReader<Object>();
		DataFileStream<?> dataStreamReader = new DataFileStream(body, greader);
		Schema avroSchema = dataStreamReader.getSchema();

		ParquetBufferedWriter outputFile = new ParquetBufferedWriter();

		ParquetWriter<Object> writer = AvroParquetWriter.builder(outputFile)
				.withRowGroupSize(256 * 1024 * 1024)
				.withPageSize(1024 * 1024)
				.withSchema(avroSchema)
				.withConf(new Configuration()).withCompressionCodec(codec).withValidation(false)
				.withDictionaryEncoding(false)
				.build();

		GenericRecord avroRecord = null;
		while (dataStreamReader.hasNext()) {
			avroRecord = (GenericRecord) dataStreamReader.next();
			writer.write((Record) avroRecord);
		}
		writer.close();
		dataStreamReader.close();

		return new ByteArrayInputStream(outputFile.toArray());
	}

	@MediaType(value = ANY, strict = false)
	@DisplayName("Write Avro to Parquet - File")
	public InputStream writeAvroToParquet(@Optional(defaultValue = PAYLOAD) InputStream body,
			@DisplayName("File Output Location") @org.mule.runtime.extension.api.annotation.param.display.Path(type = FILE, location = EXTERNAL) String parquetFilePath,
			@Optional(defaultValue = "UNCOMPRESSED") @DisplayName("Compression Codec") CompressionCodecName codec)
			throws IOException {

		GenericDatumReader<Object> greader = new GenericDatumReader<Object>();
		DataFileStream<?> dataStreamReader = new DataFileStream(body, greader);

		// convert Avro schema to Parquet schema
		Schema avroSchema = dataStreamReader.getSchema();

		java.nio.file.Path outputPath = Paths.get(parquetFilePath);

		final ParquetWriter<GenericData.Record> writer = createParquetWriterInstance(avroSchema, outputPath, codec);

		GenericRecord avroRecord = null;
		while (dataStreamReader.hasNext()) {
			avroRecord = (GenericRecord) dataStreamReader.next();
			writer.write((Record) avroRecord);
		}
		writer.close();
		dataStreamReader.close();

		return body;
	}

	private static ParquetWriter<GenericData.Record> createParquetWriterInstance(@Nonnull final Schema schema,
			@Nonnull final java.nio.file.Path fileToWrite, CompressionCodecName codec) throws IOException {
		return AvroParquetWriter.<GenericData.Record>builder(nioPathToOutputFile(fileToWrite))
				.withRowGroupSize(256 * 1024 * 1024).withPageSize(1024 * 1024).withSchema(schema)
				.withConf(new Configuration()).withCompressionCodec(codec).withValidation(false)
				.withDictionaryEncoding(false).build();
	}

	@MediaType(value = MediaType.APPLICATION_JSON, strict = false)
	@DisplayName("Read Parquet - File")
	public String readParquet(
			@DisplayName("Parquet File Location") @org.mule.runtime.extension.api.annotation.param.display.Path(type = FILE, location = EXTERNAL) String parquetFilePath) {

		ParquetReader<SimpleRecord> reader = null;
		JsonArray array = new JsonArray();
		JsonParser parser = new JsonParser();
		String item = null;

		try {
			reader = ParquetReader.builder(new SimpleReadSupport(), new Path(parquetFilePath)).build();
			ParquetMetadata metadata = ParquetFileReader.readFooter(new Configuration(), new Path(parquetFilePath));

			JsonRecordFormatter.JsonGroupFormatter formatter = JsonRecordFormatter
					.fromSchema(metadata.getFileMetaData().getSchema());

			for (SimpleRecord value = reader.read(); value != null; value = reader.read()) {
				item = formatter.formatRecord(value);
				JsonObject jsonObject = (JsonObject) parser.parse(item);
				array.add(jsonObject);
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return array.toString();
	}

	@MediaType(value = MediaType.APPLICATION_JSON, strict = false)
	@DisplayName("Get Parquet Schema - Stream")
	public String getParquetSchema(@Optional(defaultValue = PAYLOAD) InputStream body) {

		String item = null;
		String schema = null;

		Configuration conf = new Configuration();
		conf.setBoolean(org.apache.parquet.avro.AvroReadSupport.READ_INT96_AS_FIXED, true);

		try {
			ParquetBufferedReader inputFile = new ParquetBufferedReader(item, body);
			ParquetReader<GenericRecord> r = AvroParquetReader.<GenericRecord>builder(inputFile)
					.disableCompatibility()
					.withConf(conf)
					.build();
			GenericRecord firstRecord = r.read();
			if (firstRecord == null) {
				throw new IOException("Can't process empty Parquet file");
			}
			schema = firstRecord.getSchema().toString(true);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return schema;
	}

	@MediaType(value = MediaType.APPLICATION_JSON, strict = false)
	@DisplayName("Read Parquet - Stream")
	public String readParquetStream(@Optional(defaultValue = PAYLOAD) InputStream body) {

		String item = null;
		List<String> records = new ArrayList<>();

		try {
			ParquetBufferedReader inputFile = new ParquetBufferedReader(item, body);

			Configuration conf = new Configuration();
			conf.setBoolean(org.apache.parquet.avro.AvroReadSupport.READ_INT96_AS_FIXED, true);

			ParquetReader<GenericRecord> r = AvroParquetReader.<GenericRecord>builder(inputFile)
					.disableCompatibility()
					.withConf(conf)
					.build();
			GenericRecord genrecord;

			while ((genrecord = r.read()) != null) {
				String jsonRecord = deserialize(genrecord.getSchema(), toByteArray(genrecord.getSchema(), genrecord)).toString();
				jsonRecord = ParquetTimestampUtils.convertInt96(jsonRecord);
				records.add(jsonRecord);
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return records.toString();
	}

	private GenericRecord deserialize(Schema schema, byte[] data) throws IOException {
		GenericData.get().addLogicalTypeConversion(new TimestampMillisConversion());
		InputStream is = new ByteArrayInputStream(data);
		Decoder decoder = DecoderFactory.get().binaryDecoder(is, null);
		DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema, schema, GenericData.get());
		return reader.read(null, decoder);
	}

	private byte[] toByteArray(Schema schema, GenericRecord genericRecord) throws IOException {
		GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
		writer.getData().addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
		writer.write(genericRecord, encoder);
		encoder.flush();
		return baos.toByteArray();
	}

	public static class TimestampMillisConversion extends Conversion<String> {
		public TimestampMillisConversion() {
			// TO DO
		}

		public Class<String> getConvertedType() {
			return String.class;
		}

		public String getLogicalTypeName() {
			return "timestamp-millis";
		}

		@Override
		public String fromLong(Long millisFromEpoch, Schema schema, LogicalType type) {
			return Instant.ofEpochMilli(millisFromEpoch).toString();
		}

		@Override
		public Long toLong(String timestamp, Schema schema, LogicalType type) {
			return new Long(timestamp);
		}
	}
}