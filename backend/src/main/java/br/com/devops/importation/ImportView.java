package br.com.devops.importation;

import java.util.List;

public record ImportView(ImportBatch batch, List<ImportItem> items) {
}
