import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:file_picker/file_picker.dart';

const _platform = MethodChannel('autotap/native');

void main() => runApp(const AutoTapApp());

class AutoTapApp extends StatelessWidget {
  const AutoTapApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'AutoTap AI',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.teal),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String status = 'Inattivo';
  String? templatePath;

  Future<void> _pickTemplate() async {
    final res = await FilePicker.platform.pickFiles(type: FileType.image);
    if (res != null && res.files.single.path != null) {
      setState(() => templatePath = res.files.single.path);
    }
  }

  Future<void> _openAccessibility() async => _platform.invokeMethod('openAccessibility');
  Future<void> _openOverlayPermission() async => _platform.invokeMethod('openOverlayPermission');

  Future<void> _start() async {
    try {
      await _platform.invokeMethod('start', {'templatePath': templatePath});
      setState(() => status = 'In esecuzione');
    } on PlatformException catch (e) { setState(() => status = 'Errore: {e.message}'); }
  }

  Future<void> _stop() async {
    try {
      await _platform.invokeMethod('stop');
      setState(() => status = 'Inattivo');
    } on PlatformException catch (e) { setState(() => status = 'Errore: {e.message}'); }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('AutoTap AI (V3 – No Autostart)')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Text('Stato: ' + status, style: const TextStyle(fontSize: 18)),
          const SizedBox(height: 12),
          if (templatePath != null) ...[
            const Text('Template selezionato:'),
            const SizedBox(height: 8),
            Image.file(File(templatePath!), height: 160, fit: BoxFit.cover),
          ],
          const SizedBox(height: 12),
          FilledButton(onPressed: _pickTemplate, child: const Text('Scegli template (immagine)')),
          const SizedBox(height: 8),
          FilledButton.tonal(onPressed: _openAccessibility, child: const Text('Apri impostazioni Accessibilità')),
          FilledButton.tonal(onPressed: _openOverlayPermission, child: const Text('Concedi permesso overlay')),
          const Spacer(),
          FilledButton(onPressed: _start, child: const Text('Avvia rilevamento + auto-tap')),
          const SizedBox(height: 8),
          FilledButton(onPressed: _stop, child: const Text('Ferma')),
        ]),
      ),
    );
  }
}
