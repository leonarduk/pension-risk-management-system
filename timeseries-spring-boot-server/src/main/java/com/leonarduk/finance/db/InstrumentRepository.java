package com.leonarduk.finance.db;

import com.leonarduk.finance.stockfeed.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String> {
}
