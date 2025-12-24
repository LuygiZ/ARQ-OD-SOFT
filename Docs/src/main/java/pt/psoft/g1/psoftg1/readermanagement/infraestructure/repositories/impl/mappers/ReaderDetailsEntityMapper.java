package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.BirthDateSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.PhoneNumberSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderDetailsSqlEntity;
import pt.psoft.g1.psoftg1.readermanagement.model.sql.ReaderNumberSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.NameSqlEntity;
import pt.psoft.g1.psoftg1.shared.model.sql.PhotoSqlEntity;
import pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl.mappers.UserEntityMapper;

@Mapper(componentModel = "spring", uses = { UserEntityMapper.class})
public interface ReaderDetailsEntityMapper
{
    ReaderDetails toModel(ReaderDetailsSqlEntity entity);

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "toPhoneNumberEntity")
    @Mapping(target = "readerNumber", source = "readerNumber", qualifiedByName = "toReaderNumberEntity")
    @Mapping(target = "marketingConsent", source = "marketingConsent")
    @Mapping(target = "thirdPartySharingConsent", source = "thirdPartySharingConsent")
    @Mapping(target = "photo", source = "photo")
    ReaderDetailsSqlEntity toEntity(ReaderDetails model);

    default String map(PhotoSqlEntity value)
    {
        return value == null ? null : value.getPhotoFile();
    }

    default String map(NameSqlEntity value)
    {
        return value == null ? null : value.getName();
    }

    @Named("toPhoneNumberEntity")
    default PhoneNumberSqlEntity toPhoneNumberEntity(String value) {
        return value == null ? null : new PhoneNumberSqlEntity(value);
    }

    @Named("toReaderNumberEntity")
    default ReaderNumberSqlEntity toReaderNumberEntity(String value) {
        return value == null ? null : new ReaderNumberSqlEntity(value);
    }

    default BirthDate map(BirthDateSqlEntity value)
    {
        return value == null ? null : new BirthDate(value.getBirthDate().toString());
    }

    @Named("toBirthDate")
    default BirthDate toBirthDate(String value)
    {
        return new BirthDate(value);
    }

    default ReaderNumber map(ReaderNumberSqlEntity value)
    {
        return value == null ? null : new ReaderNumber(value.getReaderNumber());
    }

    default ReaderNumber map(int value)
    {
        return new ReaderNumber(value);
    }

    @Named("toPhoneNumber")
    default PhoneNumber tPhoneNumber(String value)
    {
        return value == null ? null : new PhoneNumber(value);
    }
}
